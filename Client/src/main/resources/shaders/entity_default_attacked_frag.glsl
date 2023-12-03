#version 430

in vec4 f_color;
in vec2 f_uv;
in vec2 f_pos;

uniform vec2 u_originalPos;
uniform sampler2D u_txSampler;
uniform int u_tick;
uniform int u_duration;
uniform vec2 u_entitySize;

out vec4 o_color;

void main() {
	o_color = texture(u_txSampler, f_uv);
	o_color *= f_color;

	float delta = float(u_tick) / float(u_duration);

	if (delta*u_entitySize.y > (f_pos.y - u_originalPos.y) + (f_pos.x - u_originalPos.x) &&
	    delta*u_entitySize.y < (f_pos.y - u_originalPos.y) + (f_pos.x - u_originalPos.x) + 10.0
	) {
		o_color += o_color.a * vec4(5, 5, 5, 5);
	}
}
