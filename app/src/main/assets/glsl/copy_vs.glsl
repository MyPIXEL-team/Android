#version 310 es

layout(location=0)
in vec2 position;

layout(location=1)
out vec2 textureCoord;

layout(location=2)
uniform mat4 uTransform;

void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    textureCoord = (uTransform * (gl_Position + 1.0) * 0.5).xy;
}
