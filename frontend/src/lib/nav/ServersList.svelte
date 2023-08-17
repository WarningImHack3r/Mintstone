<script lang="ts">
	import { drawerStore, popup } from "@skeletonlabs/skeleton";
	import { EditIcon, MoreVerticalIcon, TrashIcon } from "svelte-feather-icons";

	function drawerClose(target: EventTarget | null) {
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
	const servers: { id: number; name: string; ip: string }[] = [
		{
			id: 0,
			name: "Server 1",
			ip: "192.168.1.19"
		},
		{
			id: 1,
			name: "Server 2",
			ip: "192.168.1.20"
		}
	];
</script>

<nav class="list-nav p-4">
	<span class="flex items-baseline justify-between py-4 pl-4">
		<h3 class="h3">Servers</h3>
		<button
			type="button"
			class="btn-sm font-bold uppercase opacity-50"
			on:click={() => (editMode = !editMode)}
		>
			{#if editMode}
				Done
			{:else}
				Edit
			{/if}
		</button>
	</span>
	<ul>
		{#each servers as server, index}
			<li>
				{#if editMode}
					<div class="list-option">
						<span class="variant-soft-tertiary badge-icon p-4">S</span>
						<span class="flex-auto">
							<dt class="font-bold">{server.name}</dt>
							<dd class="text-sm opacity-50">{server.ip}</dd>
						</span>
						{#if editMode}
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
						{/if}
					</div>
				{:else}
					<a
						href="/"
						class={server.id === 0 ? "!variant-soft-primary" : ""}
						on:click={e => drawerClose(e.target)}
					>
						<span class="variant-soft-tertiary badge-icon p-4">S</span>
						<span class="flex-auto">
							<dt class="font-bold">{server.name}</dt>
							<dd class="text-sm opacity-50">{server.ip}</dd>
						</span>
					</a>
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
</nav>
