<script lang="ts">
	import { version } from "$app/environment";
	import { onMount } from "svelte";
	import {
		AppShell,
		AppBar,
		Drawer,
		getDrawerStore,
		getModalStore,
		localStorageStore,
		Modal,
		popup
	} from "@skeletonlabs/skeleton";
	import { GithubIcon, LogOutIcon, MenuIcon, UserIcon } from "svelte-feather-icons";
	import { backendUrl } from "$lib/utils";
	import ServersList from "$lib/components/ServersList.svelte";

	// Create stores
	const drawerStore = getDrawerStore();
	const modalStore = getModalStore();

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

	// Functions
	async function getGitHubTags() {
		const res = await fetch("https://api.github.com/repos/WarningImHack3r/Mintstone/tags");
		const tags = await res.json();

		if (res.ok) {
			return tags as Tag[];
		} else {
			throw new Error(JSON.stringify(tags));
		}
	}

	onMount(() => {
		// Detect if backend is reachable
		fetch(backendUrl, {
			method: "HEAD"
		}).catch(() => {
			console.warn("Backend is not reachable!");
			// Display a modal
			modalStore.trigger({
				type: "confirm",
				title: "Backend is not reachable!",
				body: 'The application will not be able to work until the backend is reachable. Try restarting the application.<br />If the problem persists, please <a class="anchor" href="https://github.com/WarningImHack3r/Mintstone/issues/new/choose">file an issue</a> on GitHub.<br /><br />Press \'Reload\' to reload the page and try again.',
				buttonTextConfirm: "Reload",
				response: (r: boolean) => {
					if (r) {
						location.reload();
					}
				}
			});
		});
	});
</script>

<!-- Modal -->
<Modal regionBody="max-h-80 overflow-y-auto" />

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
						v{version}
					</button>
				</div>
				<div class="card z-10 w-fit shadow-xl" data-popup="versionMenu">
					<div class="bg-surface-100-800-token arrow" />
					<div class="px-4 py-2">
						<strong>v{version}</strong>
						{#await getGitHubTags()}
							<p class="text-sm opacity-75">Checking for updates...</p>
						{:then tags}
							{#if tags.length > 0}
								{#if tags[0].name === version}
									<p class="text-sm opacity-75">Up-to-date!</p>
								{:else}
									<p class="text-sm opacity-75">Update available! ({tags[0].name})</p>
									<a
										href="https://github.com/WarningImHack3r/Mintstone/releases/latest"
										target="_blank"
										class="anchor text-sm"
									>
										What's new?
									</a>
								{/if}
							{:else}
								<p class="text-sm opacity-75">No version information available</p>
							{/if}
						{:catch}
							<p class="text-sm text-red-500/75">Couldn't check for updates</p>
						{/await}
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
			<!-- <svelte:fragment slot="trail"> -->
			<!-- Profile -->
			<!-- <button
					type="button"
					class="btn-icon-md btn-icon"
					use:popup={{
						event: "click",
						target: "profileMenu"
					}}
				>
					<UserIcon />
				</button> -->

			<!-- Settings -->
			<!-- <button
					type="button"
					class="btn-icon-md btn-icon"
					on:click={() => {
						// TODO: go to /settings
					}}
				>
					<SettingsIcon />
				</button> -->
			<!-- </svelte:fragment> -->
			<div class="card z-10 w-fit p-2 shadow-xl" data-popup="profileMenu">
				<div class="bg-surface-100-800-token arrow" />
				<nav class="list-nav">
					<ul>
						<li class="child:w-full">
							<!-- TODO: go to /profile -->
							<a href="/">
								<span class="badge">
									<UserIcon />
								</span>
								<span class="flex-auto text-left">Manage Profile</span>
							</a>
						</li>
						<hr />
						<li class="child:w-full">
							<!-- TODO: go to /logout -->
							<a href="/" class="text-error-500 hover:!bg-error-backdrop-token">
								<span class="badge">
									<LogOutIcon />
								</span>
								<span class="flex-auto text-left">Log Out</span>
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
