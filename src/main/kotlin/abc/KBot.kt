package abc

import com.lineate.xonix.mind.model.*
import java.util.*
import kotlin.math.abs

/**
 * _WARNING_ Bot must have default constructor
 */
class KBot(
    private val name: String,
    private val random: Random
) : Bot {

    // required by API
    @Suppress("unused")
    constructor() : this("", Random())

    // private val neigh = listOf(Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 0))
    private var destination: Point? = null
    private var lastMove: Move? = null
    private var m = 0
    private var n = 0
    private var pid = 0

    override fun getName(): String = "Kbot$name!"
    override fun move(gs: GameState): Move {
        pid = gs.botId
        m = gs.cells.size
        n = gs.cells.first().size
        val me = gs.me
        if (me.isEmpty)
            return Move.STOP
        val head = me.head().get() // guaranteed to exist

        if (lastMove != null) {
            val (_, newHead) = calculateHeads(me.body, lastMove!!)
            // don't try to select the last move, if it is to bite itself
            if (me.contains(newHead))
                lastMove = null
        }

        var move: Move? = null
        // some attempts to move
        for (o in 1..8) {
            if (destination == null) {
                destination = calculateDestination(random, gs, head)
            } else {
                // probably reset if we achieved the destination
                // gs.field.cells[head] != Cell.Empty
                val p = destination!!
                if (abs(p.row - head.row) + abs(p.col - head.col) <= 1)
                    destination = null
            }
            // now choose the move
            if (destination != null) {
                val ri = destination!!.row - head.row
                val rj = destination!!.col - head.col
                val r = random.nextInt(4 + abs(ri) + abs(rj))
                move = if (r < 4) {
                    Move.values()[r]
                } else if (r < 4 + abs(ri)) {
                    // vertical move
                    if (ri < 0) Move.UP else Move.DOWN
                } else {
                    // horizontal move
                    if (rj < 0) Move.LEFT else Move.RIGHT
                }
                val (_, newHead) = calculateHeads(me.body, move)
                if (!me.contains(newHead))
                    break
            } else if (lastMove == null) {
                move = Move.values()[random.nextInt(4)]
                val (_, newHead) = calculateHeads(me.body, move)
                if (!me.contains(newHead))
                    break
            } else {
                // higher probability to choose the last move
                val r = random.nextInt(16)
                move = if (r < 4) Move.values()[r] else lastMove!!
                val (_, newHead) = calculateHeads(me.body, move)
                if (!me.contains(newHead))
                    break
            }
        }
        lastMove = if (move == null) Move.STOP else move
        // if after all those attempts we don't found the move, just stay
        return lastMove!!
    }

    private fun calculateDestination(random: Random, gs: GameState, head: Point): Point? {
        // put several random dots into the field, and the first empty point
        // is our destination
        for (k in 1..16) {
            val i = random.nextInt(m)
            val j = random.nextInt(n)
            if (gs.cells[i][j] == Cell.empty()) {
                val p = Point.of(i, j)
                if (p != head) {
                    return p
                }
            }
        }
        // cannot choose the destination
        return null
    }

    fun calculateHeads(me: List<Point>, move: Move): Pair<Point, Point> {
        val (di, dj) = when (move) {
            Move.RIGHT   -> Pair(0, +1)
            Move.UP      -> Pair(-1, 0)
            Move.LEFT    -> Pair(0, -1)
            Move.DOWN    -> Pair(+1, 0)
            Move.STOP    -> Pair(0, 0)
        }
        val oldHead = me.last()
        val newHead = Point.of(
            (oldHead.row + di).bound(0, m - 1),
            (oldHead.col + dj).bound(0, n - 1)
        )
        return Pair(oldHead, newHead)
    }

    private fun Int.bound(l: Int, u: Int) = when {
        this < l -> l
        this > u -> u
        else -> this
    }
}

fun main(args: Array<String>) {
    val random = Random(123)
    val gameplay = Gameplay()
    val bots = listOf(KBot("1", random), KBot("2", random))
    val botNames = bots.map { it.name }
    val mgs = gameplay.createMatch(10, 20, bots, 100L, 0.9, 0).gameState
    for (it in 0..99) {
        for (k in bots.indices) {
            val gs = gameplay.getClientGameState(mgs, k)
            val move = bots[k].move(gs)
            gameplay.step(mgs, k, move)
            println("move = " + move + " current game state = \n" +
                gameplay.describeGameState(mgs, botNames, false, false))
        }
    }
}
