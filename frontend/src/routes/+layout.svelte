<script lang="ts">
	import "../app.css";
	import {
		AppShell,
		AppBar,
		autoModeWatcher,
		Drawer,
		getDrawerStore,
		initializeStores,
		localStorageStore,
		popup,
		storePopup
	} from "@skeletonlabs/skeleton";
	import { computePosition, autoUpdate, offset, shift, flip, arrow } from "@floating-ui/dom";
	import { GithubIcon, LogOutIcon, MenuIcon, SettingsIcon, UserIcon } from "svelte-feather-icons";
	import ServersList from "$lib/nav/ServersList.svelte";

	// Handle stores
	storePopup.set({
		computePosition,
		autoUpdate,
		offset,
		shift,
		flip,
		arrow
	});
	initializeStores();
	const drawerStore = getDrawerStore();

	// Event handlers
	function openDrawerOrSidebar() {
		const shouldOpenSidebar = hiddenButton.computedStyleMap().get("display")?.toString() === "none";
		shouldOpenSidebar
			? ($sidebarOpen = true)
			: drawerStore.open({
					width: "w-72"
			  });
	}

	function sidebarClose() {
		$sidebarOpen = false;
	}

	// Variables
	let hiddenButton: HTMLElement;

	// Stores
	const sidebarOpen = localStorageStore("sidebarOpen", true);
</script>

<svelte:head>
	<!-- eslint-disable-next-line svelte/no-at-html-tags -->
	{@html `<script>${autoModeWatcher.toString()} autoModeWatcher();</script>`}
</svelte:head>

<!-- Drawer -->
<Drawer>
	<ServersList />
</Drawer>

<!-- App Shell -->
<AppShell
	regionPage="relative"
	slotPageHeader="sticky top-0 z-10"
	slotSidebarLeft="bg-surface-50-900-token w-0 {$sidebarOpen ? 'lg:w-64' : ''}"
>
	<svelte:fragment slot="pageHeader">
		<!-- App Bar -->
		<AppBar gridColumns="grid-cols-3" slotDefault="place-self-center" slotTrail="place-content-end">
			<svelte:fragment slot="lead">
				<div class="flex items-center">
					<button
						type="button"
						class="btn btn-sm mr-4 {$sidebarOpen ? 'lg:hidden' : ''}"
						on:click={openDrawerOrSidebar}
					>
						<span>
							<MenuIcon />
						</span>
					</button>
				</div>
			</svelte:fragment>
			<div class="flex items-center gap-2">
				<img src="/favicon.png" alt="Mintstone logo" class="h-6 w-6" width="42" height="48" />
				<div class="flex items-baseline">
					<h1 class="text-lg"><a href="/">Mintstone</a></h1>
					<button
						type="button"
						class="btn btn-sm text-sm opacity-50"
						use:popup={{
							event: "click",
							target: "versionMenu"
						}}
					>
						v1.0.0
					</button>
				</div>
				<div class="card w-48 shadow-xl" data-popup="versionMenu">
					<div class="bg-surface-100-800-token arrow" />
					<div class="px-4 py-2">
						<strong>v1.0.0</strong>
						<p class="text-sm opacity-75">Up-to-date!</p>
					</div>
					<hr />
					<a
						href="https://github.com/WarningImHack3r/Mintstone"
						target="_blank"
						class="bg-surface-400-500-token flex items-center gap-1 rounded-b-md px-4 py-2 hover:variant-soft-surface"
					>
						<GithubIcon class="h-4" />
						Open on GitHub
					</a>
				</div>
			</div>
			<svelte:fragment slot="trail">
				<!-- Profile -->
				<button
					type="button"
					class="btn-icon-md btn-icon"
					use:popup={{
						event: "click",
						target: "profileMenu"
					}}
				>
					<UserIcon />
				</button>

				<button type="button" class="btn-icon-md btn-icon">
					<SettingsIcon />
				</button>
			</svelte:fragment>
			<div class="card w-48 p-2 shadow-xl" data-popup="profileMenu">
				<div class="bg-surface-100-800-token arrow" />
				<nav class="list-nav">
					<ul>
						<li>
							<a href="/">
								<span class="badge">
									<UserIcon />
								</span>
								<span class="flex-auto whitespace-pre-wrap">Manage Profile</span>
							</a>
						</li>
						<hr />
						<li>
							<a href="/" class="text-error-500 hover:!bg-error-backdrop-token">
								<span class="badge">
									<LogOutIcon />
								</span>
								<span class="flex-auto">Log Out</span>
							</a>
						</li>
					</ul>
				</nav>
			</div>
		</AppBar>
	</svelte:fragment>
	<svelte:fragment slot="sidebarLeft">
		<!-- Hidden div to determine if the screen is larger than `lg` or not, thus deciding if we should open the drawer or the sidebar -->
		<div bind:this={hiddenButton} class="lg:hidden" />

		<div class="pb-0 pl-4 pt-5">
			<button type="button" class="btn btn-sm" on:click={sidebarClose}>
				<span>
					<MenuIcon />
				</span>
			</button>
		</div>
		<ServersList />
	</svelte:fragment>

	<!-- Page Route Content -->
	<slot />
</AppShell>
