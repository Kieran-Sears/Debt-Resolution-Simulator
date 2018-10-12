package simple.DebtVsAge

import scalafx.scene.Scene
import scalafx.scene.control.Tab
import scalafx.scene.control.TabPane
import scalafx.scene.layout.BorderPane
import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart._
import simple.DebtVsAge.Main.stage

object View {

  def initialiseView(timeSeries: State): Unit = {
    stage = new JFXApp.PrimaryStage {
      title = "Debt Analysis"
      scene = new Scene(800, 800) {

        val tabPane = new TabPane
        val mainPane = new BorderPane

        tabPane.tabs = graphResults(timeSeries)

        mainPane.center = tabPane
        mainPane.prefHeight = 800
        mainPane.prefWidth = 800

        root = mainPane
      }
    }
  }

  def graphResults(currentState: State) = {
    val tabA = makeLineGraphTab(
      "batches",
      currentState.history
        .foldLeft[List[(String, Double)]](Nil)((acc, state: State) =>
          acc :+ (timeToString(state.time), state.stats.batchArrears))
    )

    val tabB = makeLineGraphTab(
      "totals",
      currentState.history
        .foldLeft[List[(String, Double)]](Nil)((acc, state: State) =>
          acc :+ (timeToString(state.time), state.stats.totalArrears)))

    val tabC = makeLineGraphTab(
      "aging",
      currentState.history
        .map(state => timeToString(state.time))
        .zip((currentState.history :+ currentState).reverse
          .map(ts => ts.stats.batchArrears))
    )

    Seq(tabA, tabB, tabC)
  }

  def makeLineGraphTab(name: String, data: Seq[(String, Double)]) = {
    val pData =
      XYChart.Series[String, Number](name, ObservableBuffer(data.map {
        case (x, y) => XYChart.Data[String, Number](x, y)
      }))

    val tab = new Tab
    tab.text = name
    tab.content = new BarChart(CategoryAxis("time"),
                               NumberAxis("arrears"),
                               ObservableBuffer(pData))

    tab
  }

  def timeToString(time: Int) = time.toString + "-" + (time + Main.interval)
}
