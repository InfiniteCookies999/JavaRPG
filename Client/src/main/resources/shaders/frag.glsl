#version 430

in vec4 f_color;
in vec2 f_uv;

uniform sampler2D u_txSampler;

out vec4 o_color;

void main() {
	o_color = texture(u_txSampler, f_uv) * f_color;
}
