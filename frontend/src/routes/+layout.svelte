<script lang="ts">
	import "../app.postcss";
	import { AppShell, AppBar, autoModeWatcher } from "@skeletonlabs/skeleton";
</script>

<svelte:head>
	<!-- eslint-disable-next-line svelte/no-at-html-tags -->
	{@html `<script>${autoModeWatcher.toString()} autoModeWatcher();</script>`}
</svelte:head>

<!-- App Shell -->
<AppShell>
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar>
			<svelte:fragment slot="lead">
				<strong class="text-xl uppercase">Skeleton</strong>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<a
					class="variant-ghost-surface btn btn-sm"
					href="https://discord.gg/EXqV7W8MtY"
					target="_blank"
					rel="noreferrer"
				>
					Discord
				</a>
				<a
					class="variant-ghost-surface btn btn-sm"
					href="https://twitter.com/SkeletonUI"
					target="_blank"
					rel="noreferrer"
				>
					Twitter
				</a>
				<a
					class="variant-ghost-surface btn btn-sm"
					href="https://github.com/skeletonlabs/skeleton"
					target="_blank"
					rel="noreferrer"
				>
					GitHub
				</a>
			</svelte:fragment>
		</AppBar>
	</svelte:fragment>
	<!-- Page Route Content -->
	<slot />
</AppShell>
