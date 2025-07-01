#version 100

attribute vec2 aPosition;
attribute vec2 aTexCoordinate;
varying vec2 vTexCoordinate;

void main() {
      vTexCoordinate = aTexCoordinate;
      gl_Position = vec4(aPosition, 0.0, 1.0); // Z and W for perspective
};