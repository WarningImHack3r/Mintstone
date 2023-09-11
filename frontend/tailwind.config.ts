import { join } from "path";
import type { Config } from "tailwindcss";
import forms from "@tailwindcss/forms";
import plugin from "tailwindcss/plugin";
import { skeleton } from "@skeletonlabs/tw-plugin";

export default {
	darkMode: "class",
	content: [
		"./src/**/*.{html,js,svelte,ts}",
		join(require.resolve("@skeletonlabs/skeleton"), "../**/*.{html,js,svelte,ts}")
	],
	theme: {
		extend: {}
	},
	plugins: [
		forms,
		plugin(function ({ addVariant }) {
			addVariant("child", "& > *");
			addVariant("child-hover", "& > *:hover");
			addVariant("child-focus", "& > *:focus");
		}),
		skeleton({ themes: { preset: [{ name: "skeleton", enhancements: true }] } })
	]
} satisfies Config;
