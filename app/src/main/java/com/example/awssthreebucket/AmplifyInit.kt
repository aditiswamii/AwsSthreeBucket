package com.example.awssthreebucket

import android.content.Context
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Amplify.configure


class AmplifyInit (){

    public fun intializeAmplify(Context: Context){
        try {
    //        Amplify.addPlugin(AWSCognitoAuthPlugin())
   //         Amplify.addPlugin(AWSS3StoragePlugin())
            configure(Context)
            Log.d("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
    }
}