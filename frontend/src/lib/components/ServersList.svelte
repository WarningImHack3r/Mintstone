<script lang="ts">
	import { serversDb } from "$lib/db/stores";
	import { getDrawerStore, getModalStore, localStorageStore, popup } from "@skeletonlabs/skeleton";
	import { Edit2Icon, MoreVerticalIcon, PlusIcon, TrashIcon } from "svelte-feather-icons";
	import { api, disableTypeCheck } from "$lib/utils";

	const drawerStore = getDrawerStore();
	const modalStore = getModalStore();

	function serverSelected(target: EventTarget | null, index: number) {
		$serverIndexStore = index;

		// Dirty hack to prevent closing the drawer when clicking on the menu button
		if (target instanceof SVGElement) {
			target = target.parentElement;
		}
		if (target instanceof HTMLButtonElement && target.classList.contains("btn-icon")) {
			return;
		}

		drawerStore.close();
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
				<button
					type="button"
					class="btn-icon"
					on:click={() =>
						modalStore.trigger({
							type: "component",
							component: "addServerModal",
							title: "Add your server",
							body: "Enter your server's address and credentials to add it to Mintstone.",
							response: r => {
								if (r) {
									$serversDb = [...$serversDb, r];
									$serverIndexStore = $serversDb.length - 1;
								}
							}
						})}
				>
					<span>
						<PlusIcon class="h-4" />
					</span>
				</button>
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
							<span class="variant-soft-tertiary badge-icon p-1">
								{#await disableTypeCheck(api(`/query?${new URLSearchParams( { server: server.address } )}`))}
									{server.name[0].toUpperCase()}
								{:then query}
									{#if query.query.favicon}
										<img src={query.query.favicon} alt={server.name} />
									{:else}
										{server.name[0].toUpperCase()}
									{/if}
								{:catch}
									{server.name[0].toUpperCase()}
								{/await}
							</span>
							<span class="text-left">
								<dt class="font-bold">{server.name}</dt>
								<dd class="text-sm opacity-50">{server.address}</dd>
							</span>
							<button
								type="button"
								class="btn-icon btn-icon-lg !ml-auto !px-0"
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
							type="button"
							class={index === $serverIndexStore ? "!variant-soft-primary" : ""}
							on:click={e => serverSelected(e.target, index)}
						>
							<span class="variant-soft-tertiary badge-icon p-1">
								{#await disableTypeCheck(api(`/query?${new URLSearchParams( { server: server.address } )}`))}
									{server.name[0].toUpperCase()}
								{:then query}
									{#if query.query.favicon}
										<img src={query.query.favicon} alt={server.name} />
									{:else}
										{server.name[0].toUpperCase()}
									{/if}
								{:catch}
									{server.name[0].toUpperCase()}
								{/await}
							</span>
							<span class="text-left">
								<dt class="font-bold">{server.name}</dt>
								<dd class="text-sm opacity-50">{server.address}</dd>
							</span>
						</button>
					{/if}
					<div class="card z-10 w-fit p-4 shadow-xl" data-popup="serverMenu{index}">
						<div class="bg-surface-100-800-token arrow" />
						<nav class="list-nav">
							<ul>
								<!-- <li class="child:w-full"> -->
								<!-- TODO: go to /servers/x/edit? -->
								<!-- <a href="/">
										<span class="badge">
											<EditIcon />
										</span>
										<span class="flex-auto text-left">Edit</span>
									</a>
								</li>
								<hr /> -->
								<li class="child:w-full">
									<button
										type="button"
										class="text-error-500 hover:!bg-error-backdrop-token"
										on:click={() => {
											$serversDb = $serversDb.filter((_, i) => i !== index);
											if ($serversDb.length === 0) {
												editMode = false;
											}
										}}
									>
										<span class="badge">
											<TrashIcon />
										</span>
										<span class="flex-auto text-left">Delete</span>
									</button>
								</li>
							</ul>
						</nav>
					</div>
				</li>
			{/each}
		</ul>
	{/if}
</nav>
