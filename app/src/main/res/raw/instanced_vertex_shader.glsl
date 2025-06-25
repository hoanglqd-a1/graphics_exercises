#version 300 es
uniform vec4 uColor;
uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;
uniform mat4 uWorldRotationMatrix;
uniform bool uUseTexture;
uniform bool uUseNormal;

in vec2 aTextureCoordinate;
in vec4 aPosition;
in vec3 aNormal;
in vec4 aInstancedModelMatrixRow1;
in vec4 aInstancedModelMatrixRow2;
in vec4 aInstancedModelMatrixRow3;
in vec4 aInstancedModelMatrixRow4;

out vec3 vPosition;
out vec4 vColor;
out vec3 vNormal;
out vec2 vTextureCoordinate;

void main() {
    mat4 InstancedModelMatrix = mat4(
        aInstancedModelMatrixRow1,
        aInstancedModelMatrixRow2,
        aInstancedModelMatrixRow3,
        aInstancedModelMatrixRow4
    );
    mat4 ModelViewMatrix = uViewMatrix * InstancedModelMatrix;
    mat4 ModelViewProjectionMatrix = uProjectionMatrix * ModelViewMatrix;
    vPosition = vec3(ModelViewMatrix * aPosition);
    vColor = uColor;
    if (uUseNormal){
        vNormal = vec3(ModelViewMatrix * vec4(aNormal, 0.0));
    }
    gl_Position = ModelViewProjectionMatrix * aPosition;
    if (uUseTexture){
        vTextureCoordinate = aTextureCoordinate;
    }
    gl_PointSize = 30.0;
}