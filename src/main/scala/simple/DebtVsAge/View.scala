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

  def initialiseView(): Unit ={
    stage = new JFXApp.PrimaryStage {
      title = "Debt Analysis"
      scene = new Scene(800, 800) {

        val tabPane = new TabPane
        val mainPane = new BorderPane

        mainPane.center = tabPane
        mainPane.prefHeight = 800
        mainPane.prefWidth = 800

        root = mainPane
      }
  }

  def graphResults(timeSeries: List[State]) = {
    stage = new JFXApp.PrimaryStage {
      title = "Debt Analysis"
      scene = new Scene(800, 800) {

        val tabPane = new TabPane
        val mainPane = new BorderPane

        val tabA = makeLineGraphTab(
          "batches",
          timeSeries.map(ts => (ts.time, ts.stats.batchArrears)))

        val tabB = makeLineGraphTab(
          "totals",
          timeSeries.map(ts => (ts.time, ts.stats.totalArrears)))

        val tabC = makeLineGraphTab(
          "aging",
          timeSeries
            .map(ts => ts.time)
            .zip(timeSeries.reverse.map(ts => ts.stats.totalArrears))
        )

        tabPane.tabs = tabPane.tabs.asInstanceOf[Seq[Tab]] ++ Seq(tabA, tabB, tabC)

        mainPane.center = tabPane
        mainPane.prefHeight = 800
        mainPane.prefWidth = 800
        root = mainPane
      }
    }

    def makeLineGraphTab(name: String, data: Seq[(String, Double)]) = {
      val pData =
        XYChart.Series[String, Number](name, ObservableBuffer(data.map {
          case (x, y) => XYChart.Data[String, Number](x, y)
        }))

      val tab = new Tab
      tab.text = name
      tab.content =
        new BarChart(CategoryAxis(), NumberAxis(), ObservableBuffer(pData))

      tab
    }
  }

}
