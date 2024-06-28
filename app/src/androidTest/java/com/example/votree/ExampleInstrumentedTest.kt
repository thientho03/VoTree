import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.votree.R
import com.example.votree.users.activities.SignInActivity
import com.example.votree.users.activities.SignUpActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(SignUpActivity::class.java)

    @Test
    fun testSignUpSuccessThenOpenSignInActivity() {
        onView(withId(R.id.nameEt)).perform(typeText("User Test"), closeSoftKeyboard())
        onView(withId(R.id.usernameEt)).perform(typeText("userTest"), closeSoftKeyboard())
        onView(withId(R.id.emailEt)).perform(typeText("testAuto@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passET)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.confirmPassEt)).perform(typeText("123456"), closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        Thread.sleep(2000)

        Intents.intended(hasComponent(SignInActivity::class.java.name))
    }

    @Test
    fun testRegistrationFailure() {
        onView(withId(R.id.nameEt)).perform(typeText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.usernameEt)).perform(typeText("testuser"), closeSoftKeyboard())
        onView(withId(R.id.emailEt)).perform(typeText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.passET)).perform(typeText("password"), closeSoftKeyboard())
        onView(withId(R.id.confirmPassEt)).perform(typeText("wrongpassword"), closeSoftKeyboard())
        onView(withId(R.id.button)).perform(click())

        Thread.sleep(2000)

        Intents.assertNoUnverifiedIntents()
    }
}