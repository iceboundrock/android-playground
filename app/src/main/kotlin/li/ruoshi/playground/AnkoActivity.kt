package li.ruoshi.playground

import android.app.Activity
import android.os.Bundle
import org.jetbrains.anko.*

/**
 * Created by ruoshili on 5/8/15.
 */

public class AnkoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            val name = editText()
            button("Say Hello") {
                onClick { toast("Hello, ${name.text}!") }
            }
        }
    }

}