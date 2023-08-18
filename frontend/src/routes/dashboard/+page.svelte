<script lang="ts">
	import { onMount } from "svelte";
	import { getModalStore, localStorageStore, popup } from "@skeletonlabs/skeleton";
	import {
		AlertCircleIcon,
		BoxIcon,
		InfoIcon,
		PlusIcon,
		RefreshCwIcon,
		XCircleIcon,
		XIcon
	} from "svelte-feather-icons";
	import type { Query, QueryResult, UpdateCheck, Version } from "$lib/utils/BackendTypes";
	import { api, getMinecraftVersions } from "$lib/utils/apiCaller";
	import { serversDb } from "$lib/db/stores";
	import DashboardOverview from "$lib/components/dashboard/DashboardOverview.svelte";

	// Stores
	const modalStore = getModalStore();
	const neverServerUpdatesStore = localStorageStore("neverShowServerUpdates", false);
	const _initialArrayStore: number[] = []; // for the store to be initialized as number[] instead of never[]
	const ignoreMCUpdatesStore = localStorageStore("ignoreMinecraftUpdates", _initialArrayStore);
	const serverIndexStore = localStorageStore("serverIndex", 0);

	// Initial state
	let initialCheckDone: boolean | undefined = undefined;
	let serverVersion: Version;
	let query: QueryResult;
	$: currentServer = $serversDb.length > $serverIndexStore ? $serversDb[$serverIndexStore] : null;
	$: if (currentServer) {
		loadServer();
	}

	// Check for updates
	let updateResult: UpdateCheck;
	let closedServerVersions: Record<string, string> = {};
	$: showServerUpdates =
		updateResult && updateResult.status === "success" && updateResult.updateAvailable
			? closedServerVersions[updateResult.latestVersion] !== serverVersion.platform &&
			  !$neverServerUpdatesStore
			: !$neverServerUpdatesStore;

	// Minecraft versions
	let closedMinecraftVersions: string[] = [];

	async function loadServer() {
		if (!currentServer) return;
		initialCheckDone = false;
		try {
			serverVersion = await api<Version>(
				`/rcon/version?${new URLSearchParams({
					serverAddress: currentServer.address,
					serverPassword: currentServer.password
				})}`
			);
		} catch (error) {}
		initialCheckDone = true;

		if (serverVersion) {
			query = (
				await api<Query>(
					`/query?${new URLSearchParams({
						server: currentServer.address
					})}`
				)
			).query;
			updateResult = await api<UpdateCheck>(
				`/rcon/check-for-updates?${new URLSearchParams({
					serverAddress: currentServer.address,
					serverPassword: currentServer.password
				})}`,
				{
					method: "POST",
					headers: {
						"Content-Type": "application/json"
					},
					body: JSON.stringify({
						platform: serverVersion.platform,
						serverVersion: serverVersion.version,
						gameVersion: query.version.name.split(" ")[1]
					})
				}
			);
		}
	}

	onMount(loadServer);
</script>

<svelte:head>
	<title>Dashboard | Mintstone</title>
</svelte:head>

{#if initialCheckDone === undefined}
	<div class="flex h-full w-full flex-col items-center justify-center gap-2">
		<AlertCircleIcon size="8x" class="mb-8 opacity-50" />
		<h2 class="h2">No server yet...</h2>
		<h3 class="h4 max-w-xl text-center opacity-75">
			Add your first server now! You're only a few clicks away from great powers (and great
			responsibilities).
		</h3>
		<a href="/" class="variant-filled btn mt-8">
			<span>
				<PlusIcon />
			</span>
			<span>Add your server</span>
		</a>
	</div>
{:else if initialCheckDone === false}
	<div class="flex h-full w-full flex-col items-center justify-center gap-2">
		<h2 class="h2">Loading your server...</h2>
		<h3 class="h4 opacity-75">Hang tight! Some network requests have to be made.</h3>
	</div>
{:else if serverVersion && currentServer && query}
	{#if showServerUpdates && updateResult && updateResult.status === "success" && updateResult.updateAvailable}
		<aside class="alert variant-ghost-warning">
			<div>
				<InfoIcon />
			</div>
			<div class="alert-message">
				<p>
					An update is available for <strong>{serverVersion.platform}</strong>
					({serverVersion.version} ➜ {updateResult.latestVersion})
				</p>
			</div>
			<div class="alert-actions">
				<a
					href={updateResult.downloadUrl}
					class="variant-filled-warning btn"
					target="_blank"
					on:click={() => {
						if (updateResult.status === "success" && updateResult.updateAvailable) {
							closedServerVersions[updateResult.latestVersion] = serverVersion.platform;
						}
					}}
				>
					Download
				</a>
				<button
					type="button"
					class="variant-outline-warning btn"
					on:click={() => {
						if (updateResult.status === "success" && updateResult.updateAvailable) {
							modalStore.trigger({
								type: "alert",
								title: `Changelog for ${serverVersion.platform} ${updateResult.latestVersion}`,
								body: `
								<em>Only the changelog <strong>from your current version</strong> (${
									serverVersion.version
								}) is shown.</em>
								<hr class="!border-t-2 my-4" />
								${updateResult.changelog
									.map(
										version => `
										<div class="my-4">
											<h3 class="h4">${version.version}</h3>
											<ul>
												${version.changes
													.map(change => {
														// Replace unsafe characters
														change = change
															.replace(/&/g, "&amp;")
															.replace(/</g, "&lt;")
															.replace(/>/g, "&gt;")
															.replace(/"/g, "&quot;")
															.replace(/'/g, "&apos;");

														if (change.includes("#")) {
															// Replace issue references by GH links (org/repo#issue)
															change = change.replace(
																/([a-zA-Z0-9-_]+\/[a-zA-Z0-9-_]+)#(\d+)/g,
																'<a href="https://github.com/$1/issues/$2" class="anchor">$1#$2</a>'
															);
															// Replace PR/issues referenced by GH format (#issue)
															if (serverVersion.platform === "Paper") {
																change = change.replace(
																	/\(#(\d+)\)/g,
																	'(<a href="https://github.com/PaperMC/Paper/pull/$1" class="anchor">#$1</a>)'
																);
															}
														}
														return `<li>${change.replace(/\n/g, "<br />")}</li>`;
													})
													.join("\n")}
											</ul>
										</div>
									`
									)
									.join("<hr />")}
							`
							});
						}
					}}
				>
					See changelog
				</button>
				<button
					type="button"
					class="btn-icon"
					use:popup={{
						event: "click",
						target: "dismissServerUpdateAlert"
					}}
				>
					<XIcon />
				</button>
			</div>
		</aside>
		<div class="card w-60 p-2 shadow-xl" data-popup="dismissServerUpdateAlert">
			<div class="bg-surface-100-800-token arrow" />
			<nav class="list-nav">
				<ul>
					<li class="child:w-full">
						<button
							type="button"
							on:click={() => {
								if (updateResult.status === "success" && updateResult.updateAvailable) {
									closedServerVersions[updateResult.latestVersion] = serverVersion.platform;
								}
							}}
						>
							<span class="badge">
								<XIcon />
							</span>
							<span>Close</span>
						</button>
					</li>
					<hr />
					<li class="child:w-full">
						<button
							type="button"
							class="text-error-500 hover:!bg-error-backdrop-token"
							on:click={() =>
								modalStore.trigger({
									type: "confirm",
									title: "Are you sure?",
									body: "You will never see any update alerts again.",
									response: r => {
										if (r) {
											$neverServerUpdatesStore = true;
										}
									}
								})}
						>
							<span class="badge">
								<XCircleIcon />
							</span>
							<span>Never show again</span>
						</button>
					</li>
				</ul>
			</nav>
		</div>
	{/if}

	{#await getMinecraftVersions() then versions}
		{@const versionsString = versions.map(v => v.versionString)}
		{#if query && !closedMinecraftVersions.includes(versions[0].versionString) && !$ignoreMCUpdatesStore.includes($serverIndexStore)}
			{@const gameVersion = query.version.name.split(" ")[1]}
			{#if versionsString.includes(gameVersion) && versionsString.indexOf(gameVersion) > 0}
				{@const newestVersion = versions[0]}
				<aside class="alert variant-ghost-secondary">
					<div>
						<BoxIcon />
					</div>
					<div class="alert-message">
						<p>
							A new <strong>Minecraft</strong> version ({newestVersion.versionString}) is available!
						</p>
					</div>
					<div class="alert-actions">
						<button
							type="button"
							class="btn-icon"
							use:popup={{
								event: "click",
								target: "dismissMinecraftUpdateAlert"
							}}
						>
							<XIcon />
						</button>
					</div>
				</aside>
				<div class="card w-60 p-2 shadow-xl" data-popup="dismissMinecraftUpdateAlert">
					<div class="bg-surface-100-800-token arrow" />
					<nav class="list-nav">
						<ul>
							<li class="child:w-full">
								<button
									type="button"
									on:click={() =>
										(closedMinecraftVersions = [
											...closedMinecraftVersions,
											newestVersion.versionString
										])}
								>
									<span class="badge">
										<XIcon />
									</span>
									<span>Close</span>
								</button>
							</li>
							<hr />
							<li class="child:w-full">
								<button
									type="button"
									class="text-error-500 hover:!bg-error-backdrop-token"
									on:click={() =>
										modalStore.trigger({
											type: "confirm",
											title: "Are you sure?",
											body: "All future Minecraft updates will be ignored <strong>for this server</strong>.",
											response: r => {
												if (r) {
													$ignoreMCUpdatesStore = [...$ignoreMCUpdatesStore, $serverIndexStore];
												}
											}
										})}
								>
									<span class="badge">
										<XCircleIcon />
									</span>
									<span>Never show again</span>
								</button>
							</li>
						</ul>
					</nav>
				</div>
			{/if}
		{/if}
	{/await}

	<main class="p-8">
		<DashboardOverview instance={currentServer} platform={serverVersion} fetchedData={query} />
	</main>
{:else}
	<div class="flex h-full w-full flex-col items-center justify-center">
		<p class="pb-16 text-9xl font-bold opacity-50">:(</p>
		<h2 class="h2">Server unreachable</h2>
		<button class="variant-filled btn mt-8 gap-1" on:click={() => loadServer()}>
			<span>
				<RefreshCwIcon />
			</span>
			<span>Retry</span>
		</button>
	</div>
{/if}
