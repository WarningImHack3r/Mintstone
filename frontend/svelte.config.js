import adapter from "@sveltejs/adapter-node";
import { vitePreprocess } from "@sveltejs/kit/vite";

/** @type {import("@sveltejs/kit").Config} */
const config = {
	extensions: [".svelte"],
	preprocess: [vitePreprocess()],

	kit: {
		adapter: adapter()
	},
	vitePlugin: {
		inspector: true
	}
};

export default config;
