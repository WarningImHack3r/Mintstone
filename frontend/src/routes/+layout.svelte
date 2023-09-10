<script lang="ts">
	import "../app.css";
	import { autoModeWatcher, initializeStores, storePopup } from "@skeletonlabs/skeleton";
	import { arrow, autoUpdate, computePosition, flip, offset, shift } from "@floating-ui/dom";

	// Initialize stores
	storePopup.set({
		computePosition,
		autoUpdate,
		offset,
		shift,
		flip,
		arrow
	});
	initializeStores();
</script>

<svelte:head>
	<!-- eslint-disable-next-line svelte/no-at-html-tags -->
	{@html `<script>${autoModeWatcher.toString()} autoModeWatcher();</script>`}
</svelte:head>

<slot />
