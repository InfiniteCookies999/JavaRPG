#version 430

layout (location = 0) in vec2 v_pos;
layout (location = 1) in vec4 v_color;
layout (location = 2) in vec2 v_uv;

out vec4 f_color;
out vec2 f_uv;
out vec2 f_pos;

uniform mat4 u_proj;

void main() {

	gl_Position = u_proj * vec4(v_pos, 0.0, 1.0);

	f_uv = vec2(v_uv.x, 1.0 - v_uv.y);
	f_color = v_color;
	f_pos = v_pos;

}
