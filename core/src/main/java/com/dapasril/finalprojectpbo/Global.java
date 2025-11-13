package com.dapasril.finalprojectpbo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Input;

public class Global {
  // Delta Time
  public static float deltaTime;

  // Input
  public static float mouseDeltaX;
  public static float mouseDeltaY;

  public static float inputHorizontal = 0f;
  public static float inputVertical = 0f;
  public static float keyboardSensitivity = 2f;

  // Assets
  public static AssetManager assets = new AssetManager();

  public static void update() {
    deltaTime = Gdx.graphics.getDeltaTime();

    mouseDeltaX = -Gdx.input.getDeltaX();
    mouseDeltaY = -Gdx.input.getDeltaY();

    // Getting the input
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      inputVertical = 1f;
    } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      inputVertical = -1f;
    } else {
      inputVertical = 0f;
    }

    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      inputHorizontal = -1f;
    } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      inputHorizontal = 1f;
    } else {
      inputHorizontal = 0f;
    }
  }
}
