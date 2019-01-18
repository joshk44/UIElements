package com.joseferreyra.usefulcomponents

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity() {


    //This is a harcoded set of images to test the library.
    private val avatars: Array<String> = arrayOf(
            "https://images.pexels.com/photos/96938/pexels-photo-96938.jpeg",
            "https://images.pexels.com/photos/320014/pexels-photo-320014.jpeg",
            "https://images.pexels.com/photos/730896/pexels-photo-730896.jpeg",
            "https://images.pexels.com/photos/20787/pexels-photo.jpg",
            "https://images.pexels.com/photos/416160/pexels-photo-416160.jpeg",
            "https://kittenrescue.org/wp-content/uploads/2017/03/KittenRescue_KittenCareHandbook.jpg",
            "https://www.profiletalent.com.au/wp-content/uploads/2017/05/profile-talent-ant-simpson-feature.jpg",
            "https://previews.123rf.com/images/duben/duben1110/duben111000007/10915538-lion-cub-profile.jpg"
    )

    private var currentAvatar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        loadNextAvatar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                avatarDemo.setImageURL(avatars[currentAvatar])
                currentAvatar = ++currentAvatar % avatars.size
            }
        })
    }

    override fun onStart() {
        super.onStart()
        avatarDemo.setImageURL(avatars[currentAvatar++])
    }
}
