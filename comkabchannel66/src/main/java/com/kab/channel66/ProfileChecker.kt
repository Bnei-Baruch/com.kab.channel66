package com.kab.channel66;

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog // Or MaterialAlertDialogBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.io.IOException

// Assume this class has access to a Context (e.g., it's an Activity or has a reference)
class ProfileChecker(private val context: Context) {

    lateinit var  endSession: userSub

    interface userSub{
         fun requestLogout()
    }
    // You'll need to obtain the userId and potentially an authToken
    fun checkUserProfileAndShowPopupIfInactive(
        userId: String,
        authToken: String?,
        endSession: userSub
    ) {
        this.endSession = endSession
        // **IMPORTANT: This function itself should be called from a background thread!**
        // Example using a simple Thread for demonstration (use Coroutines or Executors in real app)
        Thread {
            val isActive = isUserActiveSynchronous(userId, authToken)

            // Switch back to the main thread to show UI (Dialog)
            Handler(Looper.getMainLooper()).post {
                if (!isActive) {
                    showInactiveProfilePopup()
                } else {
                    // Optional: Handle the active case, e.g., log or proceed
                    println("User $userId is active.")
                }
            }
        }.start()
    }


    private fun isUserActiveSynchronous(userId: String, authToken: String?): Boolean {
        val client = OkHttpClient()
        val gson = Gson()

        val url = "https://api.kli.one/profile/v1/profile/$userId/short"

        val requestBuilder = Request.Builder().url(url)


        // Add Authorization header if you have a token
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        // Add any other necessary headers
        // requestBuilder.addHeader("X-Api-Key", "YOUR_API_KEY")


        val request = requestBuilder.build()

        try {
            val response: Response = client.newCall(request).execute() // Synchronous call

            if (!response.isSuccessful) {
                println("API Error: ${response.code} - ${response.message}")
                // Consider more specific error handling based on response code
                return false // Or throw an exception
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                println("API Error: Empty response body")
                return false
            }

            println("API Response: $responseBody") // For debugging

            // Parse the JSON response
            return try {
                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                // Assuming the structure is {"data": {"active": "true" or "false" or boolean}}
                // Adjust parsing based on the actual JSON structure


                if (jsonObject != null && jsonObject.has("active")) {
                    val activeElement = jsonObject.get("active")
                    // Handle if "active" is a boolean or a string "true"/"false"
                    if (activeElement.isJsonPrimitive && activeElement.asJsonPrimitive.isBoolean) {
                        activeElement.asBoolean
                    } else if (activeElement.isJsonPrimitive && activeElement.asJsonPrimitive.isString) {
                        activeElement.asString.lowercase() != "false"
                    } else {
                        println("API Warning: 'active' field is not a boolean or expected string.")
                        false // Default to inactive if type is unexpected
                    }
                } else {
                    println("API Warning: 'data' object or 'active' field not found in response.")
                    false // Default to inactive if field is missing
                }
            } catch (e: JsonSyntaxException) {
                println("API Error: Failed to parse JSON response - ${e.message}")
                false
            } catch (e: IllegalStateException) {
                println("API Error: JSON structure incorrect (e.g. 'active' not a primitive) - ${e.message}")
                false
            }

        } catch (e: IOException) {
            println("Network Error: ${e.message}")
            // Handle network errors (e.g., no internet, timeout)
            return false
        }
    }

    private fun showInactiveProfilePopup() {
        AlertDialog.Builder(context)
            .setTitle("User permissions")
            .setMessage("Please contact help@kli.one")
            .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    endSession.requestLogout()
            }
            .setCancelable(false) // Optional: Prevent dismissing by tapping outside
            .show()
    }
}