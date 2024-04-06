#version 330

layout (location=0) in vec3 position;

out vec3 fPos;
out vec3 wPos;

uniform mat4 viewProj;
uniform vec3 wPosition;

void main()
{
    gl_Position = viewProj * vec4(position + wPosition, 1.0);
    
    fPos = position;
    wPos = wPosition;
}