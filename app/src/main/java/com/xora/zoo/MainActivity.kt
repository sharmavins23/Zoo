package com.xora.zoo

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment // AR Fragment
    private lateinit var selectedObject: Asset // Currently selected object
    private lateinit var allObjects: JSONArray
    private var currentObject: Int = 0

    private var fileServerURL: String = "http://192.168.1.39:3000/zoo" // Fileserver URL from localhost

    // Keep track of our tracking states
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ARFragment
        arFragment = sceneform_fragment as ArFragment

        // Load models from server
        requestAssets()
        changeCurrentModel(0) // Set the default model

        // Add listener to scene view
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
            onUpdate()
        }

        // Set the onclick listener to place our object
        pinButton.setOnClickListener {
            addObject(selectedObject)
        }

        // Listener for arrow buttons
        rightButton.setOnClickListener {
            changeCurrentModel(1)
        }
        leftButton.setOnClickListener {
            changeCurrentModel(-1)
        }

        showFab(false)
    }

    // Function to retrieve JSON objects and create a JSON array
    private fun requestAssets() {
        var requestTask: String =
            NetworkAsyncCall(this@MainActivity, fileServerURL, RequestHandler.GET).execute().get()!! // This should never be null
        allObjects = ParseJson(requestTask).getJSONArray("assets")
    }

    // Function to change current model
    private fun changeCurrentModel(itr: Int) {
        currentObject += itr

        // Updating current object pointer
        if (currentObject < 0) {
            currentObject += allObjects.length()
        } else if (currentObject >= allObjects.length()) {
            currentObject -= allObjects.length()
        }

        selectedObject = Asset(allObjects.getJSONObject(currentObject).toString())
        Toast.makeText(this@MainActivity, getEmoji(selectedObject.emoji_int), Toast.LENGTH_SHORT).show()
    }

    // Return the unicode integer conversion
    private fun getEmoji(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    // Function to toggle visibility of our place object button
    private fun showFab(enabled: Boolean) {
        if (enabled) {
            pinButton.isEnabled = true
            pinButton.visibility = View.VISIBLE
        } else {
            pinButton.isEnabled = false
            pinButton.visibility = View.GONE
        }
    }

    // Updates the tracking state
    private fun onUpdate() {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                showFab(isHitting)
            }
        }
    }

    // Performs frame.HitTest and returns if a hit is detected
    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    // Makes use of ARCore's camera state and returns true if the tracking state has changed
    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        frame ?: return false // Added: probably breaks things
        isTracking = frame.camera.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    // Simply returns the center of the screen
    private fun getScreenCenter(): Point {
        val view = findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    /**
     * @param model The object of our 3D file
     *
     * This method takes in our 3D model and performs a hit test to determine where to place it
     */
    private fun addObject(model: Asset) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor from the hit test
     * @param model our 3D model of choice (in this case from our remote url)
     *
     * Uses the ARCore anchor from the hitTest result and builds the Sceneform nodes.
     * It starts the asynchronous loading of the 3D model using the ModelRenderable builder.
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Asset) {
        ModelRenderable.builder()
            .setSource(fragment.context, RenderableSource.builder().setSource(
                fragment.context,
                Uri.parse(model.asset_url),
                RenderableSource.SourceType.GLTF2
            )
                .setScale(0.025f)
                .build())
            .setRegistryId(model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, anchor, it)
            }
            .exceptionally {
                Toast.makeText(this@MainActivity, "Could not fetch model from $model", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor
     * @param renderable our model created as a Sceneform Renderable
     *
     * This method builds two nodes and attaches them to our scene
     * The Anchor nodes is positioned based on the pose of an ARCore Anchor. They stay positioned in the sample place relative to the real world.
     * The Transformable node is our Model
     * Once the nodes are connected we select the TransformableNode so it is available for interactions
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        // TransformableNode means the user to move, scale and rotate the model
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }
}