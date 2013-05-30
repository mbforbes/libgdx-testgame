package com.mortrag.lsb.testgame;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Game implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;
	
	// more textures for example game
	Texture dropImage, bucketImage;
	Sound dropSound;
	Music rainMusic;
	
	// keeping track of stuff
	Rectangle bucket;
	Array<Rectangle> raindrops;
	float w;
	float h;
	long lastDropTime; // this is bad programming practice...
	
	@Override
	public void create() {		
		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		
		//camera = new OrthographicCamera(1, h/w);
		camera = new OrthographicCamera(w, h);
		batch = new SpriteBatch();
		
		texture = new Texture(Gdx.files.internal("images/libgdx.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
		
		sprite = new Sprite(region);
		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
		
		// more for example game
		// load images
		dropImage = new Texture(Gdx.files.internal("images/drop.png"));
		bucketImage = new Texture(Gdx.files.internal("images/bucket.png"));		
		
		// load sounds
		dropSound = Gdx.audio.newSound(Gdx.files.internal("sound/drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/rain.mp3"));
		
		// start background playback immediately
		rainMusic.setLooping(true);
		rainMusic.play();
		
		// bucket
		bucket = new Rectangle();
		bucket.width = 48;
		bucket.height = 48;
		bucket.x = - bucket.width / 2;
		bucket.y = -h/2 + 20;
		
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}
	
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.width = 33;
		raindrop.height = 48;
		raindrop.x = MathUtils.random(-w/2, w/2 - raindrop.width);

		raindrop.y = h/2 - raindrop.height;
		
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}

	@Override
	public void render() {		
		draw();
		update();		
	}
	
	private void draw() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// not drawing dumb logo
		//sprite.draw(batch);
		
		// draw bucket
		batch.draw(bucketImage, bucket.x, bucket.y);
		
		// draw drops
		for (Rectangle raindrop : raindrops) {			
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		
		batch.end();
	}
	
	private void update() {
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - (bucket.width / 2);
		}
		
		if (TimeUtils.nanoTime() - lastDropTime > 50000000) {
			spawnRaindrop();
		}
		
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 48 < -h/2) {
				iter.remove();
			}
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
