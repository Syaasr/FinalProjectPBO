package com.dapasril.finalprojectpbo.scene;

import com.dapasril.finalprojectpbo.entity.HelicopterGenerator;

public class DemoWorld extends OurScene {

  HelicopterGenerator player;

  public DemoWorld() {
    super("Demo World");

    this.player = new HelicopterGenerator(true);
  }

  @Override
  public void init() {

  }

  @Override
  public void update() {

  }

  @Override
  public void dispose() {

  }
}
