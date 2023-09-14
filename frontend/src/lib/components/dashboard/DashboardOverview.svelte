<script lang="ts">
	import { createEventDispatcher, onMount } from "svelte";
	import type { QueryResult, Server, Version } from "$lib/utils/BackendTypes";
	import { api, minecraftVersionFromProtocol } from "$lib/utils";
	import { ProgressRadial, getModalStore } from "@skeletonlabs/skeleton";
	import { PowerIcon, RefreshCwIcon, UsersIcon } from "svelte-feather-icons";

	export let instance: Server;
	export let platform: Version | undefined = undefined;
	export let fetchedData: QueryResult;

	const dispatch = createEventDispatcher<{
		"server-stopped": undefined;
	}>();
	const modalStore = getModalStore();
	let badges = [platform?.platform.toString()].filter(x => x);

	let isStopping = false;
	let isReloading = false;

	onMount(async () => {
		const gameVersion =
			(await minecraftVersionFromProtocol(fetchedData.version.protocol)) ??
			fetchedData.version.name.split(" ")[1];
		badges = [...badges, gameVersion];
	});
</script>

<div class="flex items-center justify-between">
	<!-- Left part -->
	<div class="flex items-center gap-4">
		<!-- Logo -->
		<div class="card rounded-full p-4 shadow-md">
			<img src={fetchedData.favicon} alt="Logo for {instance.name}" />
		</div>
		<!-- Info -->
		<div class="flex flex-col gap-2">
			<div class="flex items-baseline gap-4">
				<!-- TODO: go to /servers/x -->
				<!-- <a href="/" class="h2 font-bold underline-offset-4 hover:underline">{instance.name}</a> -->
				<h2 class="h2">{instance.name}</h2>
				<span class="flex items-center gap-2 opacity-50">
					{fetchedData.players.online} / {fetchedData.players.max}
					<UsersIcon class="inline-block h-4 w-4" />
				</span>
			</div>
			<div class="flex gap-2">
				{#each badges as badge}
					<div class="variant-filled badge">{badge}</div>
				{/each}
			</div>
			<div class="mt-2 flex flex-col opacity-50">
				<span><strong>Address:</strong> {fetchedData.server.hostname}</span>
			</div>
		</div>
	</div>

	<!-- Right part -->
	{#if platform}
		<div class="card flex flex-col gap-2 rounded-3xl p-4 shadow-xl">
			<button
				type="button"
				class="variant-filled-error btn"
				class:relative={isStopping}
				on:click={() =>
					modalStore.trigger({
						type: "confirm",
						title: "Stop server",
						body: "Are you sure you want to stop this server? The connection will be lost until you start it again manually.",
						async response(r) {
							if (r) {
								isStopping = true;
								await api(
									`/rcon/stop?${new URLSearchParams({
										serverAddress: instance.address,
										serverPort: instance.rconPort.toString(),
										serverPassword: instance.rconPassword
									})}`,
									{
										method: "POST"
									}
								);
								isStopping = false;
								setTimeout(() => dispatch("server-stopped"), 1500);
							}
						}
					})}
			>
				{#if isStopping}
					<ProgressRadial
						stroke={100}
						track="stroke-surface-500/70"
						width="w-8"
						class="!absolute"
					/>
				{/if}
				<span class:invisible={isStopping}>
					<PowerIcon />
				</span>
				<span class:invisible={isStopping}>Stop</span>
			</button>
			<button
				type="button"
				class="variant-filled btn"
				class:relative={isReloading}
				on:click={() =>
					modalStore.trigger({
						type: "confirm",
						title: "Reload server",
						body: "Are you sure you want to reload this server? It's often not useful and might cause issues.",
						async response(r) {
							if (r) {
								isReloading = true;
								await api(
									`/rcon/reload?${new URLSearchParams({
										serverAddress: instance.address,
										serverPort: instance.rconPort.toString(),
										serverPassword: instance.rconPassword
									})}`,
									{
										method: "POST"
									}
								);
								isReloading = false;
							}
						}
					})}
			>
				{#if isReloading}
					<ProgressRadial
						stroke={100}
						meter="stroke-surface-700 dark:stroke-surface-400"
						width="w-8"
						class="!absolute"
					/>
				{/if}
				<span class:invisible={isReloading}>
					<RefreshCwIcon />
				</span>
				<span class:invisible={isReloading}>Reload</span>
			</button>
		</div>
	{/if}
</div>
