//import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
//import simple.DebtVsAge.Main
//import javafx.{stage => jfxst}
//import org.testfx.api.FxToolkit
//import scalafx.application.JFXApp
//
//
//class MainSpec extends WordSpecLike with Matchers with BeforeAndAfterAll {
//
//  override def beforeAll = {
//    JFXApp.
//  }
//
//
//  "Main" should {
//    "run for only the designated amount of time" in {
//      Main.currentTime shouldBe 0
//      Main.main(Array())
//      Main.currentTime >= Main.totalSimulationRunTime shouldBe true
//      Main.
//    }
//  }
//}
//
//class JFXAppAdapter(
//                     val jfxAppFixture: JFXAppFixture
//                   ) extends javafx.application.Application {
//
//  override def init() {
//    jfxAppFixture.init()
//  }
//
//  override def start(stage: jfxst.Stage) {
//    JFXApp.Stage = stage
//    jfxAppFixture.start(new JFXApp.PrimaryStage)
//  }
//
//  override def stop() {
//    FxToolkit.hideStage()
//    jfxAppFixture.stop()
//  }
