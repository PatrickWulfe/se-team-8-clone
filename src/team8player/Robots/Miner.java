package team8player.Robots;

import battlecode.common.*;
import team8player.*;
import static team8player.Globals.*;

import java.util.LinkedList;

public class Miner extends Unit {
    static final RobotType[] spawnList = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    /**
     * Robot constructor
     * @return a Miner
     */
    public Miner() {
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnable() {
        return spawnList[(int) (Math.random() * spawnList.length)];
    }

    /**
     * Attempts to refine soup in a given direction.
     * /
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        }
        return false;
    }


    @Override
    public void run() throws GameActionException {
        super.run();

        if (countRefinery < 15)
            for (Direction dir : Direction.allDirections())
                if (PlayerBot.tryBuild(RobotType.REFINERY, dir)) {
                    countRefinery++;
                }

        //tryBlockchain();
        // If last action wasn't mining, update goalLocs every n turns

        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : Direction.values())
            if (tryMine(dir)) {
                // Check if this is the first time mining here
                if (lastAction != 0) {
                    MapLocation loc = rc.getLocation();
                    // Check if we have put this location on the blockchain and send it if not
                    int[] filter = {teamCode, MSG_SOUP_LOCATION, loc.x, loc.y, -1, -1, -1};
                    LinkedList<int[]> tmp = Blockchain.getMessages(1, filter);
                    if (!tmp.isEmpty()) {
                        if (tmp.get(0)[0] != teamCode) {
                            Blockchain.sendSoupLoc(loc, 10);
                        }
                    }
                }
                System.out.println("I mined soup! " + rc.getSoupCarrying());
                lastAction = 0;
            } else {
                if (lastAction == 0) { // Add the location to used up goalLocs
                    MapLocation loc = rc.getLocation();
                    goalLocs.remove(loc);
                    usedLocs.add(loc);
                }
            }
        for (Direction dir : Direction.allDirections()) {
            if (tryRefine(dir)) {
                System.out.println("I refined soup! " + rc.getTeamSoup());
                if (countDesignSchool < 15 && turnCount < 200) {
                    if (turnCount < 50) {
                        for (Direction d : Direction.allDirections())
                            if (PlayerBot.tryBuild(RobotType.DESIGN_SCHOOL, d)) {
                                countDesignSchool++;
                                System.out.println("Design school created");
                            }
                    }
                }
                for (Direction d : Direction.allDirections())
                    if (PlayerBot.tryBuild(RobotType.REFINERY, d)) {
                        countRefinery++;
                        System.out.println("Refinery created");
                        lastAction = 1;
                    }
                for (Direction d : Direction.allDirections())
                    if (PlayerBot.tryBuild(RobotType.DESIGN_SCHOOL, d)) {
                        countDesignSchool++;
                        System.out.println("Design school created");
                        lastAction = 2;
                    }
                for (Direction d : Direction.allDirections())
                    if (PlayerBot.tryBuild(RobotType.LANDSCAPER, d)) {
                        System.out.println("Landscaper created");
                        lastAction = 3;
                    }
                if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
                    Direction toHQ = rc.getLocation().directionTo(HqLocation);
                    if (Unit.tryMove(toHQ)) {
                        System.out.println("move to HQ");
                        lastAction = 4;
                    }
                }
                Unit.tryMove(PlayerBot.randomDirection());
                if (Unit.tryMove(PlayerBot.randomDirection())) {
                    System.out.println("I moved!");
                    for (Direction d : Direction.allDirections()) {
                        PlayerBot.tryBuild(RobotType.FULFILLMENT_CENTER, d);

                        Unit.tryMove(PlayerBot.randomDirection());
                        lastAction = 4;
                    }
                    for (Direction d : Direction.allDirections()) {
                        PlayerBot.tryBuild(RobotType.FULFILLMENT_CENTER, d);
                        lastAction = 5;
                    }
                }
            }
        }
    }




}
