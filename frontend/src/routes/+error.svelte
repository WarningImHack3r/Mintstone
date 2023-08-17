<script lang="ts">
	import { page } from "$app/stores";
</script>

<div class="flex h-full w-full flex-col items-center justify-center">
	<p class="pb-16 text-7xl font-bold opacity-50">{$page.status}</p>
	<h2 class="h2">{$page.error?.message}</h2>
</div>
