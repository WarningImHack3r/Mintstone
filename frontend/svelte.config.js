import adapter from "@sveltejs/adapter-node";
import { vitePreprocess } from "@sveltejs/kit/vite";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";

// https://www.reddit.com/r/sveltejs/comments/rg2tt4/comment/j6jh03s/
const file = fileURLToPath(new URL("package.json", import.meta.url));
const json = readFileSync(file, "utf8");
const pkg = JSON.parse(json);

/** @type {import("@sveltejs/kit").Config} */
const config = {
	extensions: [".svelte"],
	preprocess: [vitePreprocess()],

	kit: {
		adapter: adapter(),
		version: {
			name: pkg.version
		}
	},
	vitePlugin: {
		inspector: true
	}
};

export default config;
