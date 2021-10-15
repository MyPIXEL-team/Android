#version 310 es

#extension GL_OES_EGL_image_external_essl3 : require

precision lowp float;

layout(binding=0)
uniform samplerExternalOES uTextureOES;

layout(location=1)
in vec2 textureCoord;

out vec4 color;

void main()
{
    color = texture(uTextureOES, textureCoord);
}
