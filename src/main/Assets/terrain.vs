#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;

out vec3 fPos;
out vec3 fNormal;

uniform mat4 viewProj;

void main()
{
    gl_Position = viewProj * vec4(position, 1.0);
    
    fPos = position;
    fNormal = normal;
}