<script lang="ts">
	import "../app.postcss";
	import { AppShell, AppBar, autoModeWatcher, Drawer, drawerStore } from "@skeletonlabs/skeleton";
	import ServersList from "$lib/Navigation/ServersList.svelte";

	function drawerOpen() {
		drawerStore.open({});
	}
</script>

<svelte:head>
	<!-- eslint-disable-next-line svelte/no-at-html-tags -->
	{@html `<script>${autoModeWatcher.toString()} autoModeWatcher();</script>`}
</svelte:head>

<Drawer>
	<ServersList />
</Drawer>

<!-- App Shell -->
<AppShell
	regionPage="relative"
	slotPageHeader="sticky top-0 z-10"
	slotSidebarLeft="bg-surface-500/5 w-0 lg:w-64"
>
	<svelte:fragment slot="pageHeader">
		<!-- App Bar -->
		<AppBar gridColumns="grid-cols-3" slotDefault="place-self-center" slotTrail="place-content-end">
			<svelte:fragment slot="lead">
				<div class="flex items-center">
					<button class="btn btn-sm mr-4 lg:hidden" on:click={drawerOpen}>
						<span>
							<svg viewBox="0 0 100 80" class="fill-token h-4 w-4">
								<rect width="100" height="20" />
								<rect y="30" width="100" height="20" />
								<rect y="60" width="100" height="20" />
							</svg>
						</span>
					</button>
				</div>
			</svelte:fragment>
			Mintstone
		</AppBar>
	</svelte:fragment>
	<svelte:fragment slot="sidebarLeft">
		<ServersList />
	</svelte:fragment>
	<!-- Page Route Content -->
	<slot />
</AppShell>
