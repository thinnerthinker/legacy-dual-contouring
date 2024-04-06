#version 330

in vec3 fPos;
in vec3 fNormal;

out vec4 fragColor;

vec4 color = vec4(255.0 / 255, 145.0 / 255, 0.0 / 255, 1.0);

vec4 ambientColor = vec4(1.0, 1.0, 1.0, 1.0);
float ambientIntensity = 0.5f;

vec4 diffuseColor = vec4(1.0, 1.0, 1.0, 1.0);
vec3 lightPos = vec3(-10.0, -8.0, 6.5);

void main()
{
    vec4 ambient = ambientColor * ambientIntensity;
    vec4 diffuse = max(dot(fNormal, normalize(fPos - lightPos)), 0) * diffuseColor;

    fragColor = color * (diffuse + ambient);
    fragColor.a = 1.0;
}