/*-------------------------------------------------------------------------*\
**  ScalaCheck                                                             **
**  Copyright (c) 2007-2009 Rickard Nilsson. All rights reserved.          **
**  http://www.scalacheck.org                                              **
**                                                                         **
**  This software is released under the terms of the Revised BSD License.  **
**  There is NO WARRANTY. See the file LICENSE for the full text.          **
\*-------------------------------------------------------------------------*/

package org.scalacheck

import util.{FreqMap,Buildable,Builder}

sealed abstract class Arbitrary[T] {
  val arbitrary: Gen[T]
}

/** Defines implicit <code>Arbitrary</code> instances for common types.
 *  <p>
 *  ScalaCheck
 *  uses implicit <code>Arbitrary</code> instances when creating properties
 *  out of functions with the <code>Prop.property</code> method, and when
 *  the <code>Arbitrary.arbitrary</code> method is used. For example, the
 *  following code requires that there exists an implicit
 *  <code>Arbitrary[MyClass]</code> instance:
 *  </p>
 *
 *  <p>
 *  <code>
 *    val myProp = Prop.forAll { myClass: MyClass =&gt;<br />
 *      ...<br />
 *    }<br />
 *
 *    val myGen = Arbitrary.arbitrary[MyClass]
 *  </code>
 *  </p>
 *
 *  <p>
 *  The required implicit definition could look like this:
 *  </p>
 *
 *  <p>
 *  <code>
 *    implicit val arbMyClass: Arbitrary[MyClass] = Arbitrary(...)
 *  </code>
 *  </p>
 *
 *  <p>
 *  The factory method <code>Arbitrary(...)</code> takes a generator of type
 *  <code>Gen[T]</code> and returns an instance of <code>Arbitrary[T]</code>.
 *  </p>
 *
 *  <p>
 *  The <code>Arbitrary</code> module defines implicit <code>Arbitrary</code>
 *  instances for common types, for convenient use in your properties and
 *  generators.
 *  </p>
 */
object Arbitrary {

  import Gen.{value, choose, sized, listOf, listOf1,
    frequency, oneOf, containerOf, resize}
  import util.StdRand
  import scala.collection.{immutable, mutable}
  import java.util.Date

  /** Creates an Arbitrary instance */
  def apply[T](g: => Gen[T]): Arbitrary[T] = new Arbitrary[T] {
    lazy val arbitrary = g
  }

  /** Returns an arbitrary generator for the type T. */
  def arbitrary[T](implicit a: Arbitrary[T]): Gen[T] = a.arbitrary


  // Arbitrary instances for common types //


  // Primitive types //

  /** Arbitrary instance of Boolean */
  implicit lazy val arbBool: Arbitrary[Boolean] =
    Arbitrary(oneOf(true,false))

  /** Arbitrary instance of Int */
  implicit lazy val arbInt: Arbitrary[Int] =
    Arbitrary(sized(s => choose(-s,s)))

  /** Arbitrary instance of Long */
  implicit lazy val arbLong: Arbitrary[Long] =
    Arbitrary(sized { s: Int =>
      val l = s * 10000L
      choose(-l,l)
    })

  /** Arbitrary instance of Date */
  implicit lazy val arbDate: Arbitrary[Date] = Arbitrary(for {
    l <- arbitrary[Long]
    d = new Date
  } yield new Date(d.getTime + l))

  /** Arbitrary instance of Throwable */
  implicit lazy val arbThrowable: Arbitrary[Throwable] =
    Arbitrary(value(new Exception))

  /** Arbitrary instance of Double */
  implicit lazy val arbDouble: Arbitrary[Double] =
    Arbitrary(sized(s => choose(-s:Double,s:Double)))

  /** Arbitrary instance of char */
  implicit lazy val arbChar: Arbitrary[Char] =
    Arbitrary(choose(0,255).map(_.toChar))

  /** Arbitrary instance of byte */
  implicit lazy val arbByte: Arbitrary[Byte] =
    Arbitrary(arbitrary[Int].map(_.toByte))

  /** Arbitrary instance of string */
  implicit lazy val arbString: Arbitrary[String] =
    Arbitrary(arbitrary[List[Char]].map(List.toString(_)))

  /** Generates an arbitrary property */
  implicit lazy val arbProp: Arbitrary[Prop] =
    Arbitrary(frequency(
      (5, Prop.proved),
      (4, Prop.falsified),
      (2, Prop.undecided),
      (1, Prop.exception(null))
    ))

  /** Arbitrary instance of test params */
  implicit lazy val arbTestParams: Arbitrary[Test.Params] =
    Arbitrary(for {
      minSuccTests <- choose(10,150)
      maxDiscTests <- choose(100,500)
      minSize <- choose(0,500)
      sizeDiff <- choose(0,500)
      maxSize <- choose(minSize, minSize + sizeDiff)
    } yield Test.Params(minSuccTests,maxDiscTests,minSize,maxSize,StdRand,1,0))

  /** Arbitrary instance of gen params */
  implicit lazy val arbGenParams: Arbitrary[Gen.Params] =
    Arbitrary(for {
      size <- arbitrary[Int] suchThat (_ >= 0)
    } yield Gen.Params(size, StdRand))

  /** Arbitrary instance of prop params */
  implicit lazy val arbPropParams: Arbitrary[Prop.Params] =
    Arbitrary(for {
      genPrms <- arbitrary[Gen.Params]
    } yield Prop.Params(genPrms, FreqMap.empty[immutable.Set[Any]]))


  // Higher-order types //

  /** Arbitrary instance of Gen */
  implicit def arbGen[T](implicit a: Arbitrary[T]): Arbitrary[Gen[T]] =
    Arbitrary(frequency(
      (5, arbitrary[T] map (value(_))),
      (1, Gen.fail)
    ))

  /** Arbitrary instance of option type */
  implicit def arbOption[T](implicit a: Arbitrary[T]): Arbitrary[Option[T]] =
    Arbitrary(sized(n => if(n == 0) value(None) else resize(n - 1, arbitrary[T]).map(Some(_))))

  implicit def arbEither[T, U](implicit at: Arbitrary[T], au: Arbitrary[U]): Arbitrary[Either[T, U]] =
    Arbitrary(oneOf(arbitrary[T].map(Left(_)), arbitrary[U].map(Right(_))))

  /** Arbitrary instance of immutable map */
  implicit def arbImmutableMap[T,U](implicit at: Arbitrary[T], au: Arbitrary[U]
  ): Arbitrary[immutable.Map[T,U]] = Arbitrary(
    for(seq <- arbitrary[Stream[(T,U)]]) yield immutable.Map(seq: _*)
  )

  /** Arbitrary instance of mutable map */
  implicit def arbMutableMap[T,U](implicit at: Arbitrary[T], au: Arbitrary[U]
  ): Arbitrary[mutable.Map[T,U]] = Arbitrary(
    for(seq <- arbitrary[Stream[(T,U)]]) yield mutable.Map(seq: _*)
  )

  /** Arbitrary instance of any buildable container (such as lists, arrays,
   *  streams, etc). The maximum size of the container depends on the size
   *  generation parameter. */
  //implicit def arbContainer[C[_],T](implicit a: Arbitrary[T], b: Buildable[C]
  //): Arbitrary[C[T]] = Arbitrary(containerOf[C,T](arbitrary[T]))

  // The above code crashes in Scala 2.7, therefore we must explicitly define
  // the arbitrary containers for each supported type below.

  implicit def arbList[T](implicit a: Arbitrary[T]): Arbitrary[List[T]] =
    Arbitrary(containerOf[List,T](arbitrary[T]))

  implicit def arbStream[T](implicit a: Arbitrary[T]): Arbitrary[Stream[T]] =
    Arbitrary(containerOf[Stream,T](arbitrary[T]))

  implicit def arbArray[T](implicit a: Arbitrary[T]): Arbitrary[Array[T]] =
    Arbitrary(containerOf[Array,T](arbitrary[T]))

  import scala.collection.Set
  implicit def arbSet[T](implicit a: Arbitrary[T]): Arbitrary[Set[T]] =
    Arbitrary(containerOf[Set,T](arbitrary[T]))

  import java.util.ArrayList
  implicit def arbArrayList[T](implicit a: Arbitrary[T]): Arbitrary[ArrayList[T]] =
    Arbitrary(containerOf[ArrayList,T](arbitrary[T]))


  // Functions //

  /** Arbitrary instance of Function1 */
  implicit def arbFunction1[T1,R](implicit a: Arbitrary[R]
  ): Arbitrary[T1 => R] = Arbitrary(
    for(r <- arbitrary[R]) yield (t1: T1) => r
  )

  /** Arbitrary instance of Function2 */
  implicit def arbFunction2[T1,T2,R](implicit a: Arbitrary[R]
  ): Arbitrary[(T1,T2) => R] = Arbitrary(
    for(r <- arbitrary[R]) yield (t1: T1, t2: T2) => r
  )

  /** Arbitrary instance of Function3 */
  implicit def arbFunction3[T1,T2,T3,R](implicit a: Arbitrary[R]
  ): Arbitrary[(T1,T2,T3) => R] = Arbitrary(
    for(r <- arbitrary[R]) yield (t1: T1, t2: T2, t3: T3) => r
  )

  /** Arbitrary instance of Function4 */
  implicit def arbFunction4[T1,T2,T3,T4,R](implicit a: Arbitrary[R]
  ): Arbitrary[(T1,T2,T3,T4) => R] = Arbitrary(
    for(r <- arbitrary[R]) yield (t1: T1, t2: T2, t3: T3, t4: T4) => r
  )

  /** Arbitrary instance of Function5 */
  implicit def arbFunction5[T1,T2,T3,T4,T5,R](implicit a: Arbitrary[R]
  ): Arbitrary[(T1,T2,T3,T4,T5) => R] = Arbitrary(
    for(r <- arbitrary[R]) yield (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => r
  )


  // Tuples //

  /** Arbitrary instance of 2-tuple */
  implicit def arbTuple2[T1,T2](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2]
  ): Arbitrary[(T1,T2)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
    } yield (t1,t2))

  /** Arbitrary instance of 3-tuple */
  implicit def arbTuple3[T1,T2,T3](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3]
  ): Arbitrary[(T1,T2,T3)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
    } yield (t1,t2,t3))

  /** Arbitrary instance of 4-tuple */
  implicit def arbTuple4[T1,T2,T3,T4](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4]
  ): Arbitrary[(T1,T2,T3,T4)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
    } yield (t1,t2,t3,t4))

  /** Arbitrary instance of 5-tuple */
  implicit def arbTuple5[T1,T2,T3,T4,T5](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4],
    a5: Arbitrary[T5]
  ): Arbitrary[(T1,T2,T3,T4,T5)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
      t5 <- arbitrary[T5]
    } yield (t1,t2,t3,t4,t5))

  /** Arbitrary instance of 6-tuple */
  implicit def arbTuple6[T1,T2,T3,T4,T5,T6](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4],
    a5: Arbitrary[T5], a6: Arbitrary[T6]
  ): Arbitrary[(T1,T2,T3,T4,T5,T6)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
      t5 <- arbitrary[T5]
      t6 <- arbitrary[T6]
    } yield (t1,t2,t3,t4,t5,t6))

  /** Arbitrary instance of 7-tuple */
  implicit def arbTuple7[T1,T2,T3,T4,T5,T6,T7](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4],
    a5: Arbitrary[T5], a6: Arbitrary[T6], a7: Arbitrary[T7]
  ): Arbitrary[(T1,T2,T3,T4,T5,T6,T7)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
      t5 <- arbitrary[T5]
      t6 <- arbitrary[T6]
      t7 <- arbitrary[T7]
    } yield (t1,t2,t3,t4,t5,t6,t7))

  /** Arbitrary instance of 8-tuple */
  implicit def arbTuple8[T1,T2,T3,T4,T5,T6,T7,T8](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4],
    a5: Arbitrary[T5], a6: Arbitrary[T6], a7: Arbitrary[T7], a8: Arbitrary[T8]
  ): Arbitrary[(T1,T2,T3,T4,T5,T6,T7,T8)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
      t5 <- arbitrary[T5]
      t6 <- arbitrary[T6]
      t7 <- arbitrary[T7]
      t8 <- arbitrary[T8]
    } yield (t1,t2,t3,t4,t5,t6,t7,t8))

  /** Arbitrary instance of 9-tuple */
  implicit def arbTuple9[T1,T2,T3,T4,T5,T6,T7,T8,T9](implicit
    a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4],
    a5: Arbitrary[T5], a6: Arbitrary[T6], a7: Arbitrary[T7], a8: Arbitrary[T8],
    a9: Arbitrary[T9]
  ): Arbitrary[(T1,T2,T3,T4,T5,T6,T7,T8,T9)] =
    Arbitrary(for {
      t1 <- arbitrary[T1]
      t2 <- arbitrary[T2]
      t3 <- arbitrary[T3]
      t4 <- arbitrary[T4]
      t5 <- arbitrary[T5]
      t6 <- arbitrary[T6]
      t7 <- arbitrary[T7]
      t8 <- arbitrary[T8]
      t9 <- arbitrary[T9]
    } yield (t1,t2,t3,t4,t5,t6,t7,t8,t9))


  // Constraints //

  import Constraint._

  implicit lazy val arbPosInt: Arbitrary[Pos[Int]] = Arbitrary {
    sized(max => for(n <- choose(0, max)) yield Pos(n))
  }

  implicit lazy val arbNegInt: Arbitrary[Neg[Int]] = Arbitrary {
    sized(max => for(n <- choose(-max, 0)) yield Neg(n-1))
  }

  implicit lazy val arbAlphaString: Arbitrary[Alpha[String]] = Arbitrary {
    for(cs <- listOf(Gen.alphaChar)) yield Alpha(cs.mkString)
  }

  implicit lazy val arbNumString: Arbitrary[Numeric[String]] = Arbitrary {
    for(cs <- listOf(Gen.numChar)) yield Numeric(cs.mkString)
  }

  implicit lazy val arbAlphaChar: Arbitrary[Alpha[Char]] = Arbitrary {
    for(c <- Gen.alphaChar) yield Alpha(c)
  }

  implicit lazy val arbNumChar: Arbitrary[Numeric[Char]] = Arbitrary {
    for(c <- Gen.numChar) yield Numeric(c)
  }

  //implicit def arbSmallString[S](implicit fs: S => String, a: Arbitrary[S]
  //): Arbitrary[Small[String]] = Arbitrary {
  //  for(s <- arbitrary[S]) yield Small(fs(s))
  //}

}
