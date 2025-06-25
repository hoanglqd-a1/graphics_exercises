#version 300 es
uniform vec4 uColor;
uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform bool uUseTexture;
uniform bool uUseNormal;

in vec2 aTextureCoordinate;
in vec4 aPosition;
in vec3 aNormal;

out vec3 vPosition;
out vec4 vColor;
out vec3 vNormal;
out vec2 vTextureCoordinate;

void main() {
    vPosition = vec3(uMVMatrix * aPosition);
    vColor = uColor;
    if (uUseNormal){
        vNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));
    }
    gl_Position = uMVPMatrix * aPosition;
    if (uUseTexture){
        vTextureCoordinate = aTextureCoordinate;
    }
    gl_PointSize = 30.0;
}