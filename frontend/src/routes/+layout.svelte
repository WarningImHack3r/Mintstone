<script lang="ts">
	import "../app.css";
	import {
		AppShell,
		AppBar,
		autoModeWatcher,
		Drawer,
		drawerStore,
		localStorageStore,
		popup,
		storePopup,
		ListBox,
		ListBoxItem
	} from "@skeletonlabs/skeleton";
	import { computePosition, autoUpdate, offset, shift, flip, arrow } from "@floating-ui/dom";
	import { LogOutIcon, MenuIcon, SettingsIcon, UserIcon } from "svelte-feather-icons";
	import ServersList from "$lib/nav/ServersList.svelte";

	// Enable popups
	storePopup.set({
		computePosition,
		autoUpdate,
		offset,
		shift,
		flip,
		arrow
	});

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
				<div class="flex items-baseline gap-2">
					<h1>Mintstone</h1>
					<small class="text-sm opacity-50">v1.0.0</small>
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
				<div class="card w-48 p-4 shadow-xl" data-popup="profileMenu">
					<div class="bg-surface-100-800-token arrow" />
					<ListBox>
						<ListBoxItem group="" name="profile-menu" value="profile">
							<svelte:fragment slot="lead">
								<UserIcon />
							</svelte:fragment>
							Manage Profile
						</ListBoxItem>
						<hr />
						<ListBoxItem group="" name="profile-menu" value="logout" class="text-error-500">
							<svelte:fragment slot="lead">
								<LogOutIcon />
							</svelte:fragment>
							Logout
						</ListBoxItem>
					</ListBox>
				</div>

				<button type="button" class="btn-icon-md btn-icon">
					<SettingsIcon />
				</button>
			</svelte:fragment>
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
