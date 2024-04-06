#version 330

in vec3 fPos;
in vec3 wPos;

out vec4 fragColor;

vec4 color = vec4(255.0 / 255, 0.0 / 255, 0.0 / 255, 0.5f);

void main()
{
    fragColor = color;
}