package scala.slick.jdbc

import java.sql.ResultSet

/** Represents a result set type. */
sealed abstract class ResultSetType(val intValue: Int) { self =>
  /** Run a block of code on top of a JDBC session with this result set type */
  @deprecated("Use the new Action-based API instead", "3.0")
  def apply[T](base: JdbcBackend#Session)(f: JdbcBackend#Session => T): T = f(base.forParameters(rsType = self))

  /** Run a block of code on top of the dynamic, thread-local JDBC session with this result set type */
  @deprecated("Use the new Action-based API instead", "3.0")
  def apply[T](f: => T)(implicit base: JdbcBackend#Session): T = apply(base)(_.asDynamicSession(f))

  /** Return this `ResultSetType`, unless it is `Auto` in which case
    * the specified result set type is returned instead. */
  def withDefault(r: ResultSetType) = this
}

object ResultSetType {
  /** The current result set type of the JDBC driver */
  case object Auto extends ResultSetType(ResultSet.TYPE_FORWARD_ONLY) {
    override def withDefault(r: ResultSetType) = r
  }

  /** Represents a result set type that only allows result sets to be read sequentially
    * (i.e. the cursor may only move forward). */
  case object ForwardOnly extends ResultSetType(ResultSet.TYPE_FORWARD_ONLY)

  /** Represents a result set type that allows result sets to be navigated in a
    * non-linear way while keeping the original data in the result set intact. */
  case object ScrollInsensitive extends ResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)

  /** Represents a result set type that allows result sets to be navigated in a
    * non-linear way, and changes in the underlying data to be observed. */
  case object ScrollSensitive extends ResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE)
}
