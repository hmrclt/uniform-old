
import cats.free.Free
import cats.free.Free.liftF
import cats.{Id, ~>}
import cats.data.State
import cats.effect.IO
import javax.swing._
import shapeless.{Id => _, _}

package suniform {

  sealed trait UniformA[A]
  case class Ask[A](key: String) extends UniformA[A]
}

package object suniform { 
  type Uniform[A] = Free[UniformA, A]
  def ask[A](key: String): Uniform[A] = liftF(Ask[A](key))
}

package object suniformTest {

  import suniform._

  def stupidCompiler: UniformA ~> Id = new (UniformA ~> Id) {
    import io.StdIn.readLine

    def apply[A](fa: UniformA[A]): Id[A] = {
      fa match {
        case Ask(key) =>
          readLine(s"$key: ").asInstanceOf[A]
      }
    }
  }

  trait Impl[A] {
    def to(in: UniformA[A]): IO[A]
  }

  implicit val stringImpl: Impl[String] = new Impl[String] {
    def to(in: UniformA[String]): IO[String] = IO.pure {
      JOptionPane.showInputDialog("")
    }
  }

  // implicit val intImpl: Impl[Int] = new Impl[Int] {
  //   def to(in: UniformA[Int]): IO[Int] = IO.pure {
  //     JOptionPane.showInputDialog("").toInt
  //   }
  // }


  object poly extends Poly1 {
    implicit def base[A](implicit impl: Impl[A]): Case.Aux[UniformA[A], IO[A]] = at{
      ask => impl.to(ask)
    }
  }

  def polyCompiler: UniformA ~> IO = new (UniformA ~> IO) {
    def apply[A](fa: UniformA[A]): IO[A] = ??? //poly(fa)
  }


}

  // import shapeless._
  // object polymorphSwing extends Poly1 {

  //   implicit val intCase: Case.Aux[UniformA[Int], IO[Int]] = at{
  //     k => IO.pure {
  //       JOptionPane.showInputDialog(k).toInt
  //     }
  //   }

  //   implicit val stringCase: Case.Aux[UniformA[String], IO[String]] = at{
  //     k => IO.pure {
  //       JOptionPane.showInputDialog(k)
  //     }
  //   }
  // }

  // def swingPolyCompiler: UniformA ~> IO = new (UniformA ~> IO) {
  //   def apply[A](fa: UniformA[A]): IO[A] = 
  //     polymorphSwing.apply(fa)

  // }

