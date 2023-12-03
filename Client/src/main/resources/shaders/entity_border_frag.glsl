#version 430

in vec4 f_color;
in vec2 f_uv;

uniform int u_presentBorder;
uniform vec4 u_borderColor;
uniform sampler2D u_txSampler;

out vec4 o_color;

void main() {
	o_color = texture(u_txSampler, f_uv);
	if (u_presentBorder == 1 && o_color == vec4(0, 0, 0, 1)) {
		o_color = u_borderColor;
	}
	o_color *= f_color;
}