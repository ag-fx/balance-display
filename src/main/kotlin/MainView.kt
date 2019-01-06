import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.*


/**
 * The charts displayed in the program are declared in this class.
 */
class ChartView : View() {
    private val controller: BalanceController by inject()

    override val root = VBox()

    init {
        with(root) {

            piechart ("Balance in " + controller.currencyName){
                for (item in controller.coinValues) {
                    data(item.name, item.balanceInEuro)
                }
            }
            barchart("", CategoryAxis(), NumberAxis()) {
                series("Balance in " + controller.currencyName) {
                    for (item in controller.coinValues) {
                        data(item.name, item.balanceInEuro)
                    }
                }
                /*
                series("Balance in coins") {
                    for (item in controller.coinValues) {
                        data(item.name, item.balance)
                    }
                }
                */
            }

            minWidth = 500.0

        }
    }

}

/**
 * The table displayed in the program is declared in this class.
 */
class BalanceTableView : View() {
    private val controller: BalanceController by inject()
    private val customViewModel: CustomViewModel by inject()

    override val root = tableview(customViewModel.token) {
        readonlyColumn("Name", Token::name).fixedWidth(80)
        readonlyColumn("Symbol", Token::symbol).fixedWidth(80)
        readonlyColumn("Value in " + controller.currencyName, Token::valueInEuro).fixedWidth(130)
        readonlyColumn("Balance", Token::balance).fixedWidth(80)
        readonlyColumn("Balance in " + controller.currencyName, Token::balanceInEuro).fixedWidth(130)

        minWidth = 500.0

    }

}

/**
 * This is the view of the loading screen
 */
class ProgressBarView : View() {
    private val status: TaskStatus by inject()

    override val root = StackPane()
    init {
        with(root){
            progressbar(status.progress)
            label(status.message)
        }
    }
}

/**
 * This is the view displayed after the loading screen.
 * It displays the table and charts.
 */
class MainDisplayView : View() {
    override val root = borderpane {
        left<BalanceTableView>()
        center<ChartView>()
    }
}

/**
 * This class is used to load the value of the coins in an asynchronous thread from coinmarketcap
 * and display the main program screen (MainDisplayView) afterwards.
 */
class CustomViewModel : ItemViewModel<Token>() {
    private val controller: BalanceController by inject()
    private val mainView: MainView by inject()
    val token = SimpleObjectProperty<ObservableList<Token>>()

    fun refresh() {
        runAsync {
            updateMessage("loading...")

            controller.coinValues.clear()
            controller.coinValues = controller.loadCoinValues()
        } ui {
            token.set(controller.coinValues.observable())
            mainView.replaceContent()
        }
    }

}


/**
 * This is the root element of the main view.
 * It is the outer window in which any content is displayed.
 */
class MainView : View(title = "balance display") {

    private val progressBarView: ProgressBarView by inject()
    private val mainDisplayView: MainDisplayView by inject()
    private val customViewModel: CustomViewModel by inject()

    override val root = StackPane()

    init {
        root += progressBarView
        customViewModel.refresh()
    }

    fun replaceContent() {

        root.replaceChildren(mainDisplayView)
    }

}