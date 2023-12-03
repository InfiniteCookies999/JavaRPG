#version 430

in vec4 f_color;
in vec2 f_uv;
in vec2 f_pos;

uniform vec2 u_originalPos;
uniform int u_tick;
uniform vec2 u_entitySize;
uniform sampler2D u_txSampler;

out vec4 o_color;

void main() {

	if (u_tick > 50) {
		discard;
		return;
	}
	
	o_color = texture(u_txSampler, f_uv);
	
	if (mod((f_pos.x - u_originalPos.x) * u_entitySize.x * 3.0, 7.0) > (u_tick+10)/8 &&
	    mod((f_pos.y - u_originalPos.y) * u_entitySize.y * 3.0, 7.0) > (u_tick+10)/8
	) {
		o_color += vec4(209.0/255.0, 4.0/255.0, 66.0/255.0, 0);
	} else {
		vec4 tc = o_color * vec4(0.0, 0.0, 0.0, 0.9 * 1.0/(100-u_tick)) + vec4(65.0/255.0, 1.0/255.0, 69.0/255.0, 0.0);
		o_color = vec4(0.0);
		o_color += tc;
	}
} 