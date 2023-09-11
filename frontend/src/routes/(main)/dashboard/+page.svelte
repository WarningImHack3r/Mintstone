<script lang="ts">
	import { ProgressRadial, getModalStore, localStorageStore, popup } from "@skeletonlabs/skeleton";
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
	import { api, getMinecraftVersions, minecraftVersionFromProtocol } from "$lib/utils";
	import { serversDb } from "$lib/db/stores";
	import DashboardOverview from "$lib/components/dashboard/DashboardOverview.svelte";
	import DashboardPlayerTables from "$lib/components/dashboard/DashboardTables.svelte";

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
	} else {
		initialCheckDone = undefined;
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
	let gameVersion: string;

	async function loadServer() {
		if (!currentServer) return;
		initialCheckDone = false;
		try {
			serverVersion = await api<Version>(
				`/rcon/version?${new URLSearchParams({
					serverAddress: currentServer.address,
					serverPort: currentServer.rconPort.toString(),
					serverPassword: currentServer.rconPassword
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
			gameVersion =
				(await minecraftVersionFromProtocol(query.version.protocol)) ??
				query.version.name.split(" ")[1];
			updateResult = await api<UpdateCheck>(
				`/rcon/check-for-updates?${new URLSearchParams({
					serverAddress: currentServer.address,
					serverPort: currentServer.rconPort.toString(),
					serverPassword: currentServer.rconPassword
				})}`,
				{
					method: "POST",
					headers: {
						"Content-Type": "application/json"
					},
					body: JSON.stringify({
						platform: serverVersion.platform,
						serverVersion: serverVersion.version,
						gameVersion
					})
				}
			);
		}
	}
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
		<button
			type="button"
			class="variant-filled btn mt-8"
			on:click={() =>
				modalStore.trigger({
					type: "component",
					component: "addServerModal",
					title: "Add your server",
					body: "Enter your server's address and credentials to add it to Mintstone.",
					response: r => {
						if (r) {
							$serversDb = [...$serversDb, r];
						}
					}
				})}
		>
			<span>
				<PlusIcon />
			</span>
			<span>Add your server</span>
		</button>
	</div>
{:else if initialCheckDone === false}
	<div class="flex h-full w-full flex-col items-center justify-center gap-2">
		<ProgressRadial
			stroke={100}
			meter="stroke-primary-500"
			track="stroke-primary-500/30"
			class="pb-8"
		/>
		<h2 class="h2">Loading your server...</h2>
		<h3 class="h4 text-center opacity-75">
			Hang tight! Some network requests have to be made.<br />This might take a few seconds.
		</h3>
		{#await new Promise(resolve => setTimeout(resolve, 10000)) then}
			<button
				type="button"
				class="variant-filled btn mt-8 gap-1"
				on:click={() => location.reload()}
			>
				<span>
					<RefreshCwIcon />
				</span>
				<span>Reload the page</span>
			</button>
		{/await}
	</div>
{:else if serverVersion}
	{#if currentServer && query}
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
			<div class="card z-10 w-fit p-2 shadow-xl" data-popup="dismissServerUpdateAlert">
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
			{@const versionsString = versions.map(v => v.id)}
			{#if query && !closedMinecraftVersions.includes(versions[0].id) && !$ignoreMCUpdatesStore.includes($serverIndexStore)}
				{#if versionsString.includes(gameVersion) && versionsString.indexOf(gameVersion) > 0}
					{@const newestVersion = versions[0]}
					<aside class="alert variant-ghost-secondary">
						<div>
							<BoxIcon />
						</div>
						<div class="alert-message">
							<p>
								A new <strong>Minecraft</strong> version ({newestVersion.id}) is available!
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
					<div class="card z-10 w-fit p-2 shadow-xl" data-popup="dismissMinecraftUpdateAlert">
						<div class="bg-surface-100-800-token arrow" />
						<nav class="list-nav">
							<ul>
								<li class="child:w-full">
									<button
										type="button"
										on:click={() =>
											(closedMinecraftVersions = [...closedMinecraftVersions, newestVersion.id])}
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
			<DashboardOverview
				instance={currentServer}
				platform={serverVersion}
				fetchedData={query}
				on:server-stopped={loadServer}
			/>
			<hr class="my-8 !border-t-2" />
			<div class="flex flex-col gap-4">
				<h3 class="h3">Players</h3>
				<DashboardPlayerTables server={currentServer} />
			</div>
		</main>
	<!-- {:else} -->
	<!-- TODO: placeholders? -->
	{/if}
{:else}
	<div class="flex h-full w-full flex-col items-center justify-center">
		<p class="pb-16 text-9xl font-bold opacity-50">:(</p>
		<h2 class="h2">Server unreachable</h2>
		<button type="button" class="variant-filled btn mt-8 gap-1" on:click={loadServer}>
			<span>
				<RefreshCwIcon />
			</span>
			<span>Retry</span>
		</button>
	</div>
{/if}
