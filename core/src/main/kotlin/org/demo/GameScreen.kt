package org.demo

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import kotlin.properties.Delegates


class GameScreen(private val game: Drop) : Screen {

    // load the images for the droplet and the bucket, 64x64 pixels each
    private val dropImage: Texture = Texture(Gdx.files.internal("droplet.png"))
    private val bucketImage: Texture = Texture(Gdx.files.internal("bucket.png"))

    // load the drop sound effect and the rain background "music"
    private val dropSound: Sound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"))
    private val rainMusic: Music = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"))

    private val camera: OrthographicCamera
    private val bucket: Rectangle
    private val raindrops: Array<Rectangle>
    private var lastDropTime by Delegates.notNull<Long>()
    private var dropsGathered  = 0

    init {
        rainMusic.isLooping = true

        // create the camera and the SpriteBatch
        camera = OrthographicCamera()
        camera.setToOrtho(false, 800f, 480f)

        // create a Rectangle to logically represent the bucket
        bucket = Rectangle()
        bucket.x = (800 / 2 - 64 / 2).toFloat() // center the bucket horizontally
        bucket.y = 20f // bottom left corner of the bucket is 20 pixels above

        // the bottom screen edge
        bucket.width = 64f
        bucket.height = 64f

        // create the raindrops array and spawn the first raindrop
        raindrops = Array()
        spawnRaindrop()
    }

    private fun spawnRaindrop() {
        val raindrop = Rectangle()
        raindrop.x = MathUtils.random(0, 800 - 64).toFloat()
        raindrop.y = 480f
        raindrop.width = 64f
        raindrop.height = 64f
        raindrops.add(raindrop)
        lastDropTime = TimeUtils.nanoTime()
    }

    override fun show() {
        // start the playback of the background music
        // when the screen is shown
        rainMusic.play()
    }

    override fun render(delta: Float) {
        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        ScreenUtils.clear(0f, 0f, 0.2f, 1f)

        // tell the camera to update its matrices.
        camera.update()

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.projectionMatrix = camera.combined

        // begin a new batch and draw the bucket and
        // all drops
        game.batch.begin()
        game.font.draw(game.batch, "Drops Collected: $dropsGathered", 0f, 480f)
        game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height)
        for (raindrop in raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y)
        }
        game.batch.end()


        // process user input
        if (Gdx.input.isTouched) {
            val touchPos = Vector3()
            touchPos[Gdx.input.x.toFloat(), Gdx.input.y.toFloat()] = 0f
            camera.unproject(touchPos)
            bucket.x = touchPos.x - 64 / 2
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.deltaTime
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.deltaTime


        // make sure the bucket stays within the screen bounds
        if (bucket.x < 0) bucket.x = 0f
        if (bucket.x > 800 - 64) bucket.x = (800 - 64).toFloat()


        // check if we need to create a new raindrop
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop()


        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we increase the
        // value our drops counter and add a sound effect.
        raindrops.forEachIndexed{ index, raindrop ->
            raindrop.y -= 200 * Gdx.graphics.deltaTime
            if (raindrop.y + 64 < 0) raindrops.removeIndex(index)
            if (raindrop.overlaps(bucket)) {
                dropSound.play()
                dropsGathered++
                raindrops.removeIndex(index)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
        dropImage.dispose()
        bucketImage.dispose()
        dropSound.dispose()
        rainMusic.dispose()
        game.dispose()
    }
}
