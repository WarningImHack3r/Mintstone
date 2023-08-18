<script lang="ts">
	import type { QueryResult, Server, Version } from "$lib/utils/BackendTypes";
	import { api } from "$lib/utils/apiCaller";
	import { getModalStore } from "@skeletonlabs/skeleton";
	import { PowerIcon, RefreshCwIcon, UsersIcon } from "svelte-feather-icons";

	export let instance: Server;
	export let platform: Version;
	export let fetchedData: QueryResult;

	const modalStore = getModalStore();
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
				<h2 class="h2">{instance.name}</h2>
				<span class="flex items-center gap-2 opacity-50">
					{fetchedData.players.online} / {fetchedData.players.max}
					<UsersIcon class="inline-block h-4 w-4" />
				</span>
			</div>
			<div class="flex gap-2">
				{#each [platform.platform, fetchedData.version.name.split(" ")[1]] as badge}
					<div class="variant-filled badge">{badge}</div>
				{/each}
			</div>
			<div class="mt-2 flex flex-col opacity-50">
				<span><strong>Address:</strong> {instance.address}</span>
			</div>
		</div>
	</div>

	<!-- Right part -->
	<div class="card flex flex-col gap-2 rounded-3xl p-4 shadow-xl">
		<button
			type="button"
			class="variant-filled-error btn"
			on:click={() =>
				modalStore.trigger({
					type: "confirm",
					title: "Stop server",
					body: "Are you sure you want to stop this server? The connection will be lost until you start it again manually.",
					response(r) {
						if (r) {
							api(
								`/rcon/stop?${new URLSearchParams({
									serverAddress: instance.address,
									serverPassword: instance.password
								})}`,
								{
									method: "POST"
								}
							);
						}
					}
				})}
		>
			<span>
				<PowerIcon />
			</span>
			<span>Stop</span>
		</button>
		<button
			type="button"
			class="variant-filled btn"
			on:click={() =>
				modalStore.trigger({
					type: "confirm",
					title: "Reload server",
					body: "Are you sure you want to reload this server? It's often not useful and might cause issues.",
					response(r) {
						if (r) {
							api(
								`/rcon/reload?${new URLSearchParams({
									serverAddress: instance.address,
									serverPassword: instance.password
								})}`,
								{
									method: "POST"
								}
							);
						}
					}
				})}
		>
			<span>
				<RefreshCwIcon />
			</span>
			<span>Reload</span>
		</button>
	</div>
</div>
