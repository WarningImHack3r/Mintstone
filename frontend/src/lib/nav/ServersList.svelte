<script lang="ts">
	import { serversDb } from "$lib/db/stores";
	import { getDrawerStore, localStorageStore, popup } from "@skeletonlabs/skeleton";
	import { Edit2Icon, EditIcon, MoreVerticalIcon, PlusIcon, TrashIcon } from "svelte-feather-icons";

	function serverSelected(target: EventTarget | null, index: number) {
		$serverIndexStore = index;

		// Dirty hack to prevent closing the drawer when clicking on the menu button
		if (target instanceof SVGElement) {
			target = target.parentElement;
		}
		if (target instanceof HTMLButtonElement && target.classList.contains("btn-icon")) {
			return;
		}

		getDrawerStore().close();
	}

	let editMode = false;
	const serverIndexStore = localStorageStore("serverIndex", 0);
	$: $serverIndexStore =
		$serverIndexStore > $serversDb.length && $serversDb.length > 0 ? 0 : $serverIndexStore;
</script>

<nav class="list-nav p-4">
	<span class="flex items-center justify-between py-4 pl-4">
		<h3 class="h3">Servers</h3>
		<div class="flex gap-0">
			{#if !editMode}
				<a href="/" class="btn btn-icon" on:click={() => getDrawerStore().close()}>
					<span>
						<PlusIcon class="h-4" />
					</span>
				</a>
			{/if}
			{#if $serversDb.length > 0}
				<button
					type="button"
					class={editMode ? "btn btn-sm font-bold uppercase opacity-50" : "btn btn-icon"}
					on:click={() => (editMode = !editMode)}
				>
					{#if editMode}
						Done
					{:else}
						<span>
							<Edit2Icon class="h-4" />
						</span>
					{/if}
				</button>
			{/if}
		</div>
	</span>
	{#if $serversDb.length === 0}
		<p class="text-center opacity-50">No servers yet</p>
	{:else}
		<ul>
			{#each $serversDb as server, index}
				<li class={editMode ? "" : "child:w-full"}>
					{#if editMode}
						<div class="list-option">
							<span class="variant-soft-tertiary badge-icon p-4">
								{#if server.icon}
									<img src={server.icon} alt={server.name} />
								{:else}
									{server.name.length > 0 ? server.name[0].toUpperCase() : "?"}
								{/if}
							</span>
							<span>
								<dt class="font-bold">{server.name}</dt>
								<dd class="text-sm opacity-50">{server.address}</dd>
							</span>
							<button
								type="button"
								class="btn-icon btn-icon-lg"
								use:popup={{
									event: "click",
									target: `serverMenu${index}`
								}}
							>
								<MoreVerticalIcon />
							</button>
						</div>
					{:else}
						<button
							class={index === $serverIndexStore ? "!variant-soft-primary" : ""}
							on:click={e => serverSelected(e.target, index)}
						>
							<span class="variant-soft-tertiary badge-icon p-4">
								{#if server.icon}
									<img src={server.icon} alt={server.name} />
								{:else}
									{server.name.length > 0 ? server.name[0].toUpperCase() : "?"}
								{/if}
							</span>
							<span>
								<dt class="font-bold">{server.name}</dt>
								<dd class="text-sm opacity-50">{server.address}</dd>
							</span>
						</button>
					{/if}
					<div class="card z-10 w-48 p-4 shadow-xl" data-popup="serverMenu{index}">
						<div class="bg-surface-100-800-token arrow" />
						<nav class="list-nav">
							<ul>
								<li>
									<a href="/">
										<span class="badge">
											<EditIcon />
										</span>
										<span class="flex-auto">Edit</span>
									</a>
								</li>
								<hr />
								<li>
									<a href="/" class="text-error-500 hover:!bg-error-backdrop-token">
										<span class="badge">
											<TrashIcon />
										</span>
										<span class="flex-auto">Delete</span>
									</a>
								</li>
							</ul>
						</nav>
					</div>
				</li>
			{/each}
		</ul>
	{/if}
</nav>
