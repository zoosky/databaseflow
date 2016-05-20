package services.schema

import java.sql.Clob

object JdbcHelper {
  def stringVal(a: Any, maxLength: Option[Int] = None) = a match {
    case s: String => maxLength.map(l => s.substring(0, l)).getOrElse(s)
    case c: Clob => c.getSubString(0, maxLength.getOrElse(Int.MaxValue))
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as a string.")
  }

  def intVal(a: Any) = a match {
    case i: Int => i
    case s: Short => s.toInt
    case bd: java.math.BigDecimal => bd.intValue
    case bi: java.math.BigInteger => bi.intValue
    case s: String => s.toInt
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as an int.")
  }

  def longVal(a: Any) = a match {
    case l: Long => l
    case f: Float => f.toLong
    case i: Int => i.toLong
    case bd: java.math.BigDecimal => bd.longValue
    case bi: java.math.BigInteger => bi.longValue
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as long.")
  }

  def boolVal(a: Any) = a match {
    case b: Boolean => b
    case i: Int => i > 0
    case s: Short => s > 0
    case bd: java.math.BigDecimal => bd.intValue > 0
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as a boolean.")
  }
}
