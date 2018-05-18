package abc

import com.lineate.xonix.mind.model.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

class KBot: Bot {
    private val random = Random()
    private val neigh = listOf(Pair(0, -1), Pair(-1, 0), Pair(0, 1), Pair(1, 0))
    private var destination: Point? = null
    private var lastMove: Move? = null
    private var m = 0
    private var n = 0
    private var pid = 0

    override fun getName(): String = "Kbot!"
    override fun move(gs: GameStateView): Move {
        pid = gs.botId
        m = gs.field.size
        n = gs.field.first().size
        val me = findMe(gs)
        if (me.isEmpty())
            return Move.STOP
        val head = me.first() // guaranteed to exist

        if (lastMove != null) {
            val (_, newHead) = calculateHeads(me, lastMove!!)
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
                val (_, newHead) = calculateHeads(me, move)
                if (!me.contains(newHead))
                    break
            } else if (lastMove == null) {
                move = Move.values()[random.nextInt(4)]
                val (_, newHead) = calculateHeads(me, move)
                if (!me.contains(newHead))
                    break
            } else {
                // higher probability to choose the last move
                val r = random.nextInt(16)
                move = if (r < 4) Move.values()[r] else lastMove!!
                val (_, newHead) = calculateHeads(me, move)
                if (!me.contains(newHead))
                    break
            }
        }
        lastMove = if (move == null) Move.STOP else move
        // if after all those attempts we don't found the move, just stay
        return lastMove!!
    }

    private fun findMe(gs: GameStateView): List<Point> {
        val tail = Cell.tail(pid)
        val me = mutableListOf<Point>(gs.head)
        val cp = AtomicReference<Point>(me.first())
        while (cp.get() != null) {
            // seek for lower letter around until not found
            val t = cp.get()
            val nt = neigh.map {
                val i = (t.row + it.first).bound(0, m - 1)
                val j = (t.col + it.second).bound(0, n - 1)
                Point.of(i, j)
            }.find {
                !me.contains(it) && gs.field[it.row][it.col] == tail
            }
            if (nt != null)
                me.add(0, nt)
            cp.set(nt)
        }
        return me
    }

    private fun calculateDestination(random: Random, gs: GameStateView, head: Point): Point? {
        // put several random dots into the field, and the first empty point
        // is our destination
        for (k in 1..16) {
            val i = random.nextInt(m)
            val j = random.nextInt(n)
            if (gs.field[i][j] == Cell.empty()) {
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