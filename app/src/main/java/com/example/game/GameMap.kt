package com.example.game

import com.example.R
import com.example.model.Obstacle
import com.example.model.ObstacleType
import kotlin.math.max
import kotlin.math.min

object GameMap {
    // Scaled 2x from native 1672 x 941 pixel-art city map
    const val MAP_WIDTH = 3344f
    const val MAP_HEIGHT = 1882f

    val obstacles = mutableListOf<Obstacle>()
    val spawnPoints = mutableListOf<Pair<Float, Float>>()

    init {
        // --- OUTER BOUNDARY WALLS ---
        obstacles.add(Obstacle(0f, 0f, MAP_WIDTH, 50f, ObstacleType.WALL, null, "North Wall"))
        obstacles.add(Obstacle(0f, MAP_HEIGHT - 50f, MAP_WIDTH, 50f, ObstacleType.WALL, null, "South Wall"))
        obstacles.add(Obstacle(0f, 0f, 50f, MAP_HEIGHT, ObstacleType.WALL, null, "West Wall"))
        obstacles.add(Obstacle(MAP_WIDTH - 50f, 0f, 50f, MAP_HEIGHT, ObstacleType.WALL, null, "East Wall"))

        // --- 1. HOSPITAL & EMERGENCY COMPLEX (Top-Left) ---
        // Main Hospital Building
        obstacles.add(Obstacle(60f, 50f, 440f, 320f, ObstacleType.BUILDING, null, "Civil Hospital"))
        // Hospital Emergency Annex & Canopy
        obstacles.add(Obstacle(340f, 300f, 160f, 180f, ObstacleType.BUILDING, null, "Emergency Entrance"))
        // Parked Ambulance Vehicle
        obstacles.add(Obstacle(220f, 380f, 70f, 130f, ObstacleType.AUTO_RICKSHAW, null, "Ambulance Van"))

        // --- 2. CONSTRUCTION SITE (Top-Center-Left) ---
        // Multi-story concrete frame & tower crane base
        obstacles.add(Obstacle(560f, 50f, 480f, 450f, ObstacleType.BUILDING, null, "Metro Construction Site"))
        // Heavy Construction Equipment & Dump Truck
        obstacles.add(Obstacle(840f, 410f, 130f, 90f, ObstacleType.AUTO_RICKSHAW, null, "Tipper Truck"))

        // --- 3. GYM & FITNESS CENTER (Top-Center) ---
        // Gym building with dumbbell signage
        obstacles.add(Obstacle(1080f, 50f, 220f, 460f, ObstacleType.BUILDING, null, "Desi Akhada Gym"))

        // --- 4. CENTRAL GREEN PARK (Top-Center-Right) ---
        // Dense northern park boundary fence
        obstacles.add(Obstacle(1310f, 50f, 370f, 80f, ObstacleType.WALL, null, "Park Railing"))
        // Historic Central Fountain
        obstacles.add(Obstacle(1380f, 190f, 100f, 100f, ObstacleType.BUILDING, null, "Park Fountain"))

        // --- 5. MANDIR / TEMPLE COURTYARD (Top-Right) ---
        // Golden Mandir Pagoda & Sanctum
        obstacles.add(Obstacle(1710f, 50f, 300f, 440f, ObstacleType.BUILDING, null, "Shiv Mandir"))
        // Temple Courtyard Walls
        obstacles.add(Obstacle(1670f, 380f, 70f, 130f, ObstacleType.WALL, null, "Mandir Wall West"))
        obstacles.add(Obstacle(1980f, 380f, 70f, 130f, ObstacleType.WALL, null, "Mandir Wall East"))

        // --- 6. HOTEL & CHAI TAPRI (West Mid-Street) ---
        // West Hotel Building
        obstacles.add(Obstacle(50f, 580f, 150f, 440f, ObstacleType.BUILDING, null, "City Lodging Hotel"))
        // Famous Chai Tapri Stall & Outdoor Benches
        obstacles.add(Obstacle(240f, 730f, 220f, 260f, ObstacleType.CHAI_TAPRI, null, "Raju Bhai Chai Tapri"))

        // --- 7. COMMERCIAL CENTER (General Store & Medical Pharmacy) ---
        // General Kirana Store (red & white striped awning)
        obstacles.add(Obstacle(660f, 740f, 240f, 330f, ObstacleType.BUILDING, null, "Sharma General Store"))
        // Medical Pharmacy Store (green cross sign)
        obstacles.add(Obstacle(900f, 740f, 210f, 330f, ObstacleType.BUILDING, null, "Janta Medical Store"))
        // Water Tank & Utility Shed
        obstacles.add(Obstacle(1120f, 740f, 100f, 260f, ObstacleType.BUILDING, null, "Water Supply Tank"))
        // Delivery Truck on north avenue
        obstacles.add(Obstacle(730f, 590f, 170f, 85f, ObstacleType.AUTO_RICKSHAW, null, "Delivery Truck"))
        // Parked Red Sedan in side lane
        obstacles.add(Obstacle(590f, 930f, 65f, 110f, ObstacleType.SCOOTER, null, "Red Maruti Car"))

        // --- 8. INDIAN OIL PETROL STATION (East Mid-Street) ---
        // Petrol Pumps & Overhead Fuel Canopy
        obstacles.add(Obstacle(1250f, 660f, 270f, 180f, ObstacleType.BUILDING, null, "Indian Oil Canopy"))
        // Petrol Station Office & Kiosk
        obstacles.add(Obstacle(1520f, 700f, 90f, 220f, ObstacleType.BUILDING, null, "Fuel Office Kiosk"))
        // Parked Fuel Tanker & Supply Van
        obstacles.add(Obstacle(1260f, 910f, 80f, 120f, ObstacleType.AUTO_RICKSHAW, null, "Fuel Delivery Van"))
        // Parked Auto Rickshaw at station
        obstacles.add(Obstacle(1430f, 930f, 65f, 85f, ObstacleType.AUTO_RICKSHAW, null, "Station Auto"))

        // --- 9. RAILWAY STATION & TRACKS (Bottom-Left) ---
        // Raised Passenger Platform
        obstacles.add(Obstacle(50f, 1370f, 430f, 50f, ObstacleType.WALL, null, "Railway Platform Wall"))
        // Stationary Passenger Train Cars (Orange/Blue)
        obstacles.add(Obstacle(50f, 1540f, 360f, 130f, ObstacleType.BUILDING, null, "Express Train Coaches"))
        // Trackside Storage Crates
        obstacles.add(Obstacle(340f, 1420f, 110f, 90f, ObstacleType.CRATE, null, "Railway Cargo Crates"))
        // South Railway Wall
        obstacles.add(Obstacle(50f, 1810f, 490f, 60f, ObstacleType.WALL, null, "Railway Perimeter Wall"))

        // --- 10. SCHOOL COMPLEX & PLAYGROUND (Bottom-Center) ---
        // Main School Building (red roof & signage)
        obstacles.add(Obstacle(750f, 1270f, 360f, 180f, ObstacleType.BUILDING, null, "Saraswati Vidya Mandir"))
        // School West Wall
        obstacles.add(Obstacle(570f, 1370f, 30f, 430f, ObstacleType.WALL, null, "School West Wall"))
        // School East Wall
        obstacles.add(Obstacle(1110f, 1370f, 30f, 430f, ObstacleType.WALL, null, "School East Wall"))
        // School South Wall with open pedestrian gate in center
        obstacles.add(Obstacle(570f, 1770f, 220f, 30f, ObstacleType.WALL, null, "School South Wall West"))
        obstacles.add(Obstacle(920f, 1770f, 220f, 30f, ObstacleType.WALL, null, "School South Wall East"))

        // --- 11. RESIDENTIAL MOHALLA & BAZAAR (Bottom-East) ---
        // North Residential Block
        obstacles.add(Obstacle(1220f, 1270f, 260f, 270f, ObstacleType.BUILDING, null, "Mohalla Chawl Block A"))
        // South Residential Block
        obstacles.add(Obstacle(1220f, 1590f, 260f, 260f, ObstacleType.BUILDING, null, "Mohalla Chawl Block B"))
        // East Market Chawl Rows
        obstacles.add(Obstacle(1490f, 1330f, 220f, 520f, ObstacleType.BUILDING, null, "Vegetable Market Stalls"))

        // --- 12. CANAL & DOCK WATER BODY (Far-East) ---
        // Pier Sea-Wall and Unwalkable Deep Water Body
        obstacles.add(Obstacle(1760f, 680f, 300f, 1200f, ObstacleType.WALL, null, "Canal Sea Wall & Water"))

        // --- STRATEGIC SPAWN POINTS ACROSS OPEN AVENUES & ALLEYS ---
        spawnPoints.add(Pair(120f, 500f))    // Hospital Gate Alley
        spawnPoints.add(Pair(1000f, 500f))   // Construction Avenue
        spawnPoints.add(Pair(1550f, 480f))   // Park Promenade
        spawnPoints.add(Pair(1850f, 500f))   // Temple Road
        spawnPoints.add(Pair(1680f, 1100f))  // Indian Oil Crossing
        spawnPoints.add(Pair(1180f, 1180f))  // South Market Junction
        spawnPoints.add(Pair(520f, 1250f))   // School Outer Lane
        spawnPoints.add(Pair(840f, 1820f))   // School Gate South
        spawnPoints.add(Pair(1650f, 1820f))  // Harbor Pier Entrance
        spawnPoints.add(Pair(500f, 700f))    // Chai Tapri Crossroads
    }

    /**
     * Checks if a point with given radius intersects any obstacle.
     */
    fun isCircleColliding(cx: Float, cy: Float, radius: Float): Boolean {
        for (ob in obstacles) {
            val closestX = max(ob.x, min(cx, ob.x + ob.width))
            val closestY = max(ob.y, min(cy, ob.y + ob.height))
            val dx = cx - closestX
            val dy = cy - closestY
            if ((dx * dx + dy * dy) < (radius * radius)) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a bullet point is inside any obstacle.
     */
    fun isPointBlocked(px: Float, py: Float): Boolean {
        if (px < 50f || px > MAP_WIDTH - 50f || py < 50f || py > MAP_HEIGHT - 50f) return true
        for (ob in obstacles) {
            if (px >= ob.x && px <= ob.x + ob.width && py >= ob.y && py <= ob.y + ob.height) {
                return true
            }
        }
        return false
    }

    /**
     * Resolves movement collision using separate X and Y axis testing.
     * This enables smooth wall sliding instead of stuttering or getting stuck.
     */
    fun resolveMovement(
        currentX: Float,
        currentY: Float,
        targetX: Float,
        targetY: Float,
        radius: Float
    ): Pair<Float, Float> {
        val clampedTargetX = targetX.coerceIn(radius + 50f, MAP_WIDTH - radius - 50f)
        val testX = if (!isCircleColliding(clampedTargetX, currentY, radius)) clampedTargetX else currentX

        val clampedTargetY = targetY.coerceIn(radius + 50f, MAP_HEIGHT - radius - 50f)
        val testY = if (!isCircleColliding(testX, clampedTargetY, radius)) clampedTargetY else currentY

        return Pair(testX, testY)
    }
}
