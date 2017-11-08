import cats.free.Free
import cats.free.Free.liftF
import cats.{Id, ~>}
import cats.data.State
import cats.effect.IO
import javax.swing._


package uniform {

  sealed trait UniformA[A]
  case class AskBool(key: String) extends UniformA[Boolean]
  case class AskString(key: String) extends UniformA[String]
  case class AskInt(key: String) extends UniformA[Int]    

}

package object uniform { 
  type Uniform[A] = Free[UniformA, A]
  def askBool(key: String): Uniform[Boolean] = liftF(AskBool(key))
  def askString(key: String): Uniform[String] = liftF(AskString(key))  
  def askInt(key: String): Uniform[Int] = liftF(AskInt(key))
}

package object test {
  import uniform._

  val f = for {
    name <- askString("name")
    age <- askInt("age")
    macUser <- askBool("macUser")
  } yield {
    if (macUser)
      s"Begone $name!"
    else
      s"Hello $name, you are $age years old"
  }

  def cliCompiler: UniformA ~> Id = new (UniformA ~> Id) {

    import io.StdIn.readLine

    def apply[A](fa: UniformA[A]): Id[A] = {

      fa match {
        case AskString(key) =>
          readLine(s"$key: ")
        case AskInt(key) =>
          readLine(s"$key: ").toInt
        case AskBool(key) =>
          readLine(s"$key: ").toLowerCase.startsWith("y")          
      }
    }
  }

  def swingIoCompiler: UniformA ~> IO = new (UniformA ~> IO) {

    def apply[A](fa: UniformA[A]): IO[A] = fa match {
      case AskString(k) => IO.pure {
        JOptionPane.showInputDialog(k)
      }
      case AskInt(k) => IO.pure {
        JOptionPane.showInputDialog(k).toInt
      }
      case AskBool(k) =>
        IO.pure(
          JOptionPane.showConfirmDialog(null,
            k, k, JOptionPane.YES_NO_OPTION) == 0
        )
    }
  }

  def cliIoCompiler: UniformA ~> IO = new (UniformA ~> IO) {

    def readLineIO(key: String): IO[String] =
      IO.pure(io.StdIn.readLine(s"$key: "))

    def apply[A](fa: UniformA[A]): IO[A] = fa match {
      case AskString(k) => readLineIO(k)
      case AskInt(k) => readLineIO(k).map{_.toInt}
      case AskBool(k) => readLineIO(k).map{
        _.toLowerCase.startsWith("y")
      }
    }
  }
}

object GuiApp extends App {
  import test._
  import uniform._

  val programGui = IO.pure(()).
    flatMap{_ => f.foldMap(swingIoCompiler)}.
    flatMap{x => IO.pure(JOptionPane.showMessageDialog(null, x, x, 0))}
  
  programGui.unsafeRunSync
}

object CliApp extends App {
  import test._
  import uniform._

  val programCli = IO.pure(()).
    flatMap{_ => f.foldMap(cliIoCompiler)}.
    flatMap{x => IO.pure(println(x))}
  
  programCli.unsafeRunSync
}

